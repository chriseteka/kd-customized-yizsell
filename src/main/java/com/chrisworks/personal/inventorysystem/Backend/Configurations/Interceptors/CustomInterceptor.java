package com.chrisworks.personal.inventorysystem.Backend.Configurations.Interceptors;

import com.chrisworks.personal.inventorysystem.Backend.Configurations.Interceptors.Data.STATUS;
import com.chrisworks.personal.inventorysystem.Backend.Configurations.Interceptors.Data.URIData;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static com.chrisworks.personal.inventorysystem.Backend.Configurations.SecurityConstants.*;

@Component
public class CustomInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uriKey = request.getHeader(URI_KEY);
        if ((request.getMethod().equalsIgnoreCase(POST_METHOD)
                || (request.getMethod().equalsIgnoreCase(PUT_METHOD))) && uriKey != null && !uriKey.isEmpty()){

            if (!URI_MAP.isEmpty() && URI_MAP.containsKey(uriKey)){

                URIData uriData = URI_MAP.get(uriKey);
                if (uriData.getStatus().equals(STATUS.COMPLETED)){

                    response.setStatus(uriData.getResponseStatus());
                    return false;
                }
                else return false;
            }else {

                URI_MAP.put(uriKey, new URIData());
                return true;
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        String uriKey = request.getHeader(URI_KEY);
        if ((request.getMethod().equalsIgnoreCase(POST_METHOD)
                || (request.getMethod().equalsIgnoreCase(PUT_METHOD))) && uriKey != null && !uriKey.isEmpty()){

            URIData uriData = URI_MAP.get(uriKey);
            uriData.setStatus(STATUS.COMPLETED);
            uriData.setResponseStatus(response.getStatus());
        }
    }

    public static Map<String, URIData> URI_MAP = new HashMap<>();
}
