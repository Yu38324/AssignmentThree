package com.example.assignmentthree.api;

import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class APIManager {
    private static APIManager instance;
    private RequestQueue requestQueue;
    private Context context;

    private APIManager(Context context) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
    }

    public static synchronized APIManager getInstance(Context context) {
        if (instance == null) {
            instance = new APIManager(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }
}