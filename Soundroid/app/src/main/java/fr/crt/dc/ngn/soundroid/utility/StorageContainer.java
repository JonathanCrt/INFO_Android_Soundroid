package fr.crt.dc.ngn.soundroid.utility;

import java.util.HashMap;
import java.util.Map;

import fr.crt.dc.ngn.soundroid.model.Song;

public class StorageContainer {
    private Map<Long, Object> map  = new HashMap<>();
    private long counter = 0L;
    private static StorageContainer instance = null;

    public long add(Object a){
        long id = counter++;
        map.put(id,a);
        return id;
    }

    public Object get(long id){
        return map.get(id);
    }

    public static StorageContainer getInstance(){
        if(instance == null){
            instance = new StorageContainer();
        }
        return instance;
    }


}