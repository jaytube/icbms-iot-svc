package com.icbms.iot.inbound.factory;

import com.icbms.iot.enums.GatewayRunType;
import com.icbms.iot.inbound.service.GatewayRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class GatewayRunnerFactory {

    @Autowired
    @Qualifier("singleRunner")
    private GatewayRunner singleRunner;

    @Autowired
    @Qualifier("groupRunner")
    private GatewayRunner groupRunner;

    public GatewayRunner getRunnerByType(GatewayRunType type) {
        if(type == GatewayRunType.GROUP)
            return groupRunner;
        else if(type == GatewayRunType.SINGLE)
            return singleRunner;
        else
            return null;
    }
}
