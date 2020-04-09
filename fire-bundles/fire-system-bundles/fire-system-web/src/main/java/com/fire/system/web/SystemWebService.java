package com.fire.system.web;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fire.system.web.rest.scr.ResourceService;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * WEB DEMO 入口
 * Created from huangjp on 2020/4/2 0002-下午 21:51
 *
 * @version 1.0
 * @email 262404150@qq.com
 */
@Component
public class SystemWebService {

    private Server server;

    @Activate
    public void activate() throws Exception {
        JAXRSServerFactoryBean bean = new JAXRSServerFactoryBean();
        bean.setAddress("/ur");
        bean.setBus(BusFactory.getDefaultBus());
        bean.setProvider(new JacksonJsonProvider());
        bean.setServiceBean(new ResourceService());
        server = bean.create();

        System.out.println(server.isStarted());
    }

    @Deactivate
    public void deactivate() throws Exception {
        if (server != null) {
            server.destroy();
        }
    }
}
