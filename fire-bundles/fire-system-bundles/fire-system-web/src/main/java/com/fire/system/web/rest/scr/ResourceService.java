package com.fire.system.web.rest.scr;

import com.fire.system.api.IResourceService;
import com.fire.system.api.model.UserModel;

import javax.ws.rs.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 资源实现
 * Created from huangjp on 2020/4/2 0002-下午 22:24
 *
 * @version 1.0
 * @email 262404150@qq.com
 */
@Path("/")
public class ResourceService implements IResourceService {

    private final Map<String, UserModel> bookings = new HashMap<>();

    public Map<String, UserModel> getBookings() {
        return bookings;
    }

    @Path("/")
    @Produces("application/json")
    @GET
    public Collection<UserModel> list() {
        return bookings.values();
    }

    @Path("/{id}")
    @Produces("application/json")
    @GET
    public UserModel get(@PathParam("id") Long id) {
        return bookings.get(id);
    }

    @Path("/")
    @Consumes("application/json")
    @POST
    public void add(UserModel booking) {
        bookings.put(booking.getId(), booking);
    }

    @Path("/")
    @Consumes("application/json")
    @PUT
    public void update(UserModel booking) {
        bookings.remove(booking.getId());
        bookings.put(booking.getId(), booking);
    }

    @Path("/{id}")
    @DELETE
    public void remove(@PathParam("id") Long id) {
        bookings.remove(id);
    }
}
