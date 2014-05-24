package com.devicehive.auth.websockets;

import com.devicehive.auth.HivePrincipal;
import com.devicehive.model.Device;
import com.devicehive.service.DeviceService;
import com.devicehive.util.ThreadLocalVariablesKeeper;
import com.devicehive.websockets.handlers.annotations.WebsocketController;
import com.devicehive.websockets.util.WebsocketSession;
import com.google.gson.JsonObject;

import javax.annotation.Priority;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.websocket.Session;

import static com.devicehive.configuration.Constants.DEVICE_ID;
import static com.devicehive.configuration.Constants.DEVICE_KEY;

@WebsocketController
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class AuthenticationInterceptor {

    @Inject
    private DeviceService deviceService;

    @AroundInvoke
    public Object authenticate(InvocationContext ctx) throws Exception {
        Session session = ThreadLocalVariablesKeeper.getSession();
        if (WebsocketSession.getPrincipal(session) != null) {
            ThreadLocalVariablesKeeper.setPrincipal(WebsocketSession.getPrincipal(session));
        } else {
            JsonObject request = ThreadLocalVariablesKeeper.getRequest();
            String deviceId = null, deviceKey = null;
            if (request.get(DEVICE_ID) != null) {
                deviceId = request.get(DEVICE_ID).getAsString();
            }
            if (request.get(DEVICE_KEY) != null) {
                deviceKey = request.get(DEVICE_KEY).getAsString();
            }
            if (deviceId != null && deviceKey != null) {
                Device device = deviceService.authenticate(deviceId, deviceKey);
                if (device != null) {
                    HivePrincipal principal = new HivePrincipal(null, device, null);
                    ThreadLocalVariablesKeeper.setPrincipal(principal);
                }
            }
        }
        return ctx.proceed();
    }

}