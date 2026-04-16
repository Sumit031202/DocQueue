package com.sumit.DocQueue.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationService {
    private final Map<Long, List<SseEmitter>> emitters=new ConcurrentHashMap<>();
    public SseEmitter addSubscription(Long doctorId){
        // create a new connection
        SseEmitter emitter=new SseEmitter(Long.MAX_VALUE);

        // put patients of the doctor in the right place
        emitters.computeIfAbsent(doctorId,k->new CopyOnWriteArrayList<>()).add(emitter);

        // setup the cleanup rules
        emitter.onCompletion(()-> removeEmitter(doctorId,emitter));
        emitter.onTimeout(()-> removeEmitter(doctorId,emitter));
        emitter.onError((e)-> removeEmitter(doctorId,emitter));

        return emitter;
    }

    public void removeEmitter(Long doctorId, SseEmitter emitter){
        List<SseEmitter> doctorEmitters=emitters.get(doctorId);
        if(doctorEmitters!=null){
            doctorEmitters.remove(emitter);
            if(doctorEmitters.isEmpty()){
                emitters.remove(doctorId);
            }
        }
    }

    public void sendUpdate(Long doctorId,Object updateData){
        // get the list of patients' emitters for this doctor
        List<SseEmitter> doctorEmitters=emitters.get(doctorId);

        //
        if(doctorEmitters!=null){
            for(SseEmitter emitter: doctorEmitters){
                try{
                    emitter.send(updateData);
                }catch(Exception e){
                    removeEmitter(doctorId,emitter);
                }
            }
        }
    }
}
