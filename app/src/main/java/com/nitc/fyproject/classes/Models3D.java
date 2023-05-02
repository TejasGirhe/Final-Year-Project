package com.nitc.fyproject.classes;

import java.util.HashMap;

public class Models3D {
    HashMap<String, String> hash_map = new HashMap<String, String>();

    public HashMap<String, String> getModels(){
        hash_map.put("flower",  "https://raw.githubusercontent.com/TejasGirhe/3D-Model-Libraray/main/flower/scene.gltf");
        hash_map.put("avocado", "https://raw.githubusercontent.com/TejasGirhe/3D-Models/main/avocado/scene.gltf");

        return hash_map;
    }
}
