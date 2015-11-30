package com.devicehive.websockets;

import com.devicehive.base.AbstractWebSocketTest;
import com.devicehive.base.fixture.DeviceFixture;
import com.devicehive.base.fixture.JsonFixture;
import com.devicehive.base.fixture.WebSocketFixture;
import com.devicehive.base.websocket.WebSocketSynchronousConnection;
import com.devicehive.model.Equipment;
import com.devicehive.model.Network;
import com.devicehive.model.updates.DeviceClassUpdate;
import com.devicehive.model.updates.DeviceUpdate;
import com.google.gson.JsonObject;
import org.junit.Test;
import org.springframework.web.socket.TextMessage;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static com.devicehive.base.websocket.WebSocketSynchronousConnection.WAIT_TIMEOUT;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class DeviceHandlersTest extends AbstractWebSocketTest {

    @Test
    public void should_save_device_with_key() throws Exception {
        WebSocketSynchronousConnection connection = syncConnection("/websocket/device");
        WebSocketFixture.authenticateKey(ACCESS_KEY, connection);

        Equipment equipment = DeviceFixture.createEquipment();
        DeviceClassUpdate deviceClass = DeviceFixture.createDeviceClass();
        deviceClass.setEquipment(Optional.of(Collections.singleton(equipment)));
        Network network = DeviceFixture.createNetwork();
        String deviceId = UUID.randomUUID().toString();
        String deviceKey = UUID.randomUUID().toString();
        DeviceUpdate device = DeviceFixture.createDevice(deviceKey);
        device.setDeviceClass(Optional.of(deviceClass));
        device.setNetwork(Optional.ofNullable(network));

        //device/save
        JsonObject deviceSave = JsonFixture.createWsCommand("device/save", "1", deviceId, singletonMap("device", gson.toJsonTree(device)));
        TextMessage response = connection.sendMessage(new TextMessage(gson.toJson(deviceSave)), WAIT_TIMEOUT);
        JsonObject jsonResp = gson.fromJson(response.getPayload(), JsonObject.class);
        assertThat(jsonResp.get("action").getAsString(), is("device/save"));
        assertThat(jsonResp.get("requestId").getAsString(), is("1"));
        assertThat(jsonResp.get("status").getAsString(), is("success"));


        //device/get
        JsonObject deviceGet = JsonFixture.createWsCommand("device/get", "2", deviceId);
        response =  connection.sendMessage(new TextMessage(gson.toJson(deviceGet)), WAIT_TIMEOUT);
        jsonResp = gson.fromJson(response.getPayload(), JsonObject.class);
        assertThat(jsonResp.get("action").getAsString(), is("device/get"));
        assertThat(jsonResp.get("requestId").getAsString(), is("2"));
        assertThat(jsonResp.get("status").getAsString(), is("success"));

        DeviceUpdate deviceResp = gson.fromJson(jsonResp.get("device").getAsJsonObject(), DeviceUpdate.class);
        assertThat(deviceResp.getGuid().orElse(null), is(deviceId));
        assertThat(deviceResp.getName(), is(device.getName()));
        assertThat(deviceResp.getStatus(), is(device.getStatus()));
        assertThat(deviceResp.getData().orElse(null), notNullValue());
        Network savedNetwork = deviceResp.getNetwork().orElse(null);
        assertThat(savedNetwork.getId(), notNullValue());
        assertThat(network.getName(), is(savedNetwork.getName()));
        assertThat(network.getDescription(), is(savedNetwork.getDescription()));
        DeviceClassUpdate savedClass = deviceResp.getDeviceClass().orElse(null);
        assertThat(savedClass, notNullValue());
        assertThat(savedClass.getId(), notNullValue());
        assertThat(savedClass.getName(), is(deviceClass.getName()));
        assertThat(savedClass.getVersion(), is(deviceClass.getVersion()));
        assertThat(savedClass.getPermanent(), is(deviceClass.getPermanent()));
        assertThat(savedClass.getOfflineTimeout(), is(deviceClass.getOfflineTimeout()));
        assertThat(savedClass.getData().orElse(null), notNullValue());
    }

    @Test
    public void should_save_device_as_admin() throws Exception {
        WebSocketSynchronousConnection connection = syncConnection("/websocket/device");
        WebSocketFixture.authenticateUser(ADMIN_LOGIN, ADMIN_PASS,connection);

        Equipment equipment = DeviceFixture.createEquipment();
        DeviceClassUpdate deviceClass = DeviceFixture.createDeviceClass();
        deviceClass.setEquipment(Optional.of(Collections.singleton(equipment)));
        Network network = DeviceFixture.createNetwork();
        String deviceId = UUID.randomUUID().toString();
        DeviceUpdate device = DeviceFixture.createDevice(deviceId);
        device.setDeviceClass(Optional.of(deviceClass));
        device.setNetwork(Optional.of(network));

        //device/save
        JsonObject deviceSave = JsonFixture.createWsCommand("device/save", "1", deviceId, singletonMap("device", gson.toJsonTree(device)));
        TextMessage response = connection.sendMessage(new TextMessage(gson.toJson(deviceSave)), WAIT_TIMEOUT);
        JsonObject jsonResp = gson.fromJson(response.getPayload(), JsonObject.class);
        assertThat(jsonResp.get("action").getAsString(), is("device/save"));
        assertThat(jsonResp.get("requestId").getAsString(), is("1"));
        assertThat(jsonResp.get("status").getAsString(), is("success"));


        //device/get
        JsonObject deviceGet = JsonFixture.createWsCommand("device/get", "2", deviceId);
        response =  connection.sendMessage(new TextMessage(gson.toJson(deviceGet)), WAIT_TIMEOUT);
        jsonResp = gson.fromJson(response.getPayload(), JsonObject.class);
        assertThat(jsonResp.get("action").getAsString(), is("device/get"));
        assertThat(jsonResp.get("requestId").getAsString(), is("2"));
        assertThat(jsonResp.get("status").getAsString(), is("success"));

        DeviceUpdate deviceResp = gson.fromJson(jsonResp.get("device").getAsJsonObject(), DeviceUpdate.class);
        assertThat(deviceResp.getGuid().get(), is(deviceId));
        assertThat(deviceResp.getName(), is(device.getName()));
        assertThat(deviceResp.getStatus(), is(device.getStatus()));
        assertThat(deviceResp.getData().get(), notNullValue());
        Network savedNetwork = deviceResp.getNetwork().get();
        assertThat(savedNetwork.getId(), notNullValue());
        assertThat(network.getName(), is(savedNetwork.getName()));
        assertThat(network.getDescription(), is(savedNetwork.getDescription()));
        DeviceClassUpdate savedClass = deviceResp.getDeviceClass().get();
        assertThat(savedClass, notNullValue());
        assertThat(savedClass.getId(), notNullValue());
        assertThat(savedClass.getName(), is(deviceClass.getName()));
        assertThat(savedClass.getVersion(), is(deviceClass.getVersion()));
        assertThat(savedClass.getPermanent(), is(deviceClass.getPermanent()));
        assertThat(savedClass.getOfflineTimeout(), is(deviceClass.getOfflineTimeout()));
        assertThat(savedClass.getData().get(), notNullValue());
    }

    @Test
    public void should_return_401_status_for_anonymous() throws Exception {
        WebSocketSynchronousConnection connection = syncConnection("/websocket/device");
        WebSocketFixture.authenticateKey(ACCESS_KEY, connection);

        Equipment equipment = DeviceFixture.createEquipment();
        DeviceClassUpdate deviceClass = DeviceFixture.createDeviceClass();
        deviceClass.setEquipment(Optional.of(Collections.singleton(equipment)));
        Network network = DeviceFixture.createNetwork();
        String deviceId = UUID.randomUUID().toString();
        String deviceKey = UUID.randomUUID().toString();
        DeviceUpdate device = DeviceFixture.createDevice(deviceKey);
        device.setDeviceClass(Optional.of(deviceClass));
        device.setNetwork(Optional.ofNullable(network));

        //device/save
        JsonObject deviceSave = JsonFixture.createWsCommand("device/save", "1", deviceId, singletonMap("device", gson.toJsonTree(device)));
        TextMessage response = connection.sendMessage(new TextMessage(gson.toJson(deviceSave)), WAIT_TIMEOUT);
        JsonObject jsonResp = gson.fromJson(response.getPayload(), JsonObject.class);
        assertThat(jsonResp.get("action").getAsString(), is("device/save"));
        assertThat(jsonResp.get("requestId").getAsString(), is("1"));
        assertThat(jsonResp.get("status").getAsString(), is("success"));

        connection.stop();

        connection = syncConnection("/websocket/device");
        //device/get without deviceId/deviceKey authentication
        JsonObject deviceGet = JsonFixture.createWsCommand("device/get", "2");
        response = connection.sendMessage(new TextMessage(gson.toJson(deviceGet)), WAIT_TIMEOUT);
        jsonResp = gson.fromJson(response.getPayload(), JsonObject.class);
        assertThat(jsonResp.get("action").getAsString(), is("device/get"));
        assertThat(jsonResp.get("requestId").getAsString(), is("2"));
        assertThat(jsonResp.get("status").getAsString(), is("error"));
        assertThat(jsonResp.get("code").getAsString(), is(String.valueOf(Response.Status.UNAUTHORIZED.getStatusCode())));
        assertThat(jsonResp.get("error").getAsString(), is(String.valueOf(Response.Status.UNAUTHORIZED.getReasonPhrase())));
    }

}