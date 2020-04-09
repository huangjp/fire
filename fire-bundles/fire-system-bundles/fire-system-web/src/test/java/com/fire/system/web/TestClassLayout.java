package com.fire.system.web;

import com.fire.system.api.model.UserModel;
import com.fire.system.web.rest.scr.ResourceService;
import org.junit.Test;
import org.openjdk.jol.info.ClassLayout;

import java.util.UUID;

/**
 * test
 * Created from huangjp on 2020/4/5 0005-上午 9:49
 *
 * @version 1.0
 * @email 262404150@qq.com
 */
public class TestClassLayout {


    @Test
    public void test() throws InterruptedException {
        ResourceService resourceService = new ResourceService();

        System.out.println("1" + ClassLayout.parseInstance(resourceService).toPrintable());
        synchronized (resourceService) {
            System.out.println("2" + ClassLayout.parseInstance(resourceService).toPrintable());
        }
        UserModel model = new UserModel();
        model.setId(UUID.randomUUID().toString());
        model.setLoginName("admin");
        model.setPassword("123456");
        resourceService.add(model);
//        Thread.sleep(5000);
        System.out.println("3" + ClassLayout.parseInstance(resourceService).toPrintable());

        System.out.println("4" + ClassLayout.parseInstance(resourceService.getBookings()).toPrintable());
        System.out.println("5" + ClassLayout.parseInstance(model).toPrintable());
    }
}
