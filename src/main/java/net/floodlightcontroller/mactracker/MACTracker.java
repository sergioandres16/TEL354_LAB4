//https://floodlight.atlassian.net/wiki/spaces/floodlightcontroller/pages/1343513/How+to+Write+a+Module
//https://ant.apache.org/bindownload.cgi
//https://archive.apache.org/dist/maven/maven-3/3.6.3/binaries/
//http://192.168.35.89:8080/accounts/login/?next=/
package net.floodlightcontroller.mactracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import net.floodlightcontroller.core.IFloodlightProviderService;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;
import net.floodlightcontroller.packet.Ethernet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.packet.Ethernet;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.MacAddress;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;

public class MACTracker implements IOFMessageListener, IFloodlightModule, IMACTrackerService {
    protected IFloodlightProviderService floodlightProvider;
    protected Set<Long> macAddresses;
    protected static Logger logger;
    protected IRestApiService restApi;

    @Override
    public String getName() {
// TODO Auto-generated method stub
        return MACTracker.class.getSimpleName();
    }
    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
// TODO Auto-generated method stub
        return (type.equals(OFType.PACKET_IN) && name.equals("forwarding"));
    }
    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
// TODO Auto-generated method stub
        return false;
    }
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
// TODO Auto-generated method stub
        Collection<Class<? extends IFloodlightService>> array = new ArrayList<Class<? extends
                IFloodlightService>>();
        array.add(IMACTrackerService.class);
        return array;
    }
    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
// TODO Auto-generated method stub
        Map<Class<? extends IFloodlightService>, IFloodlightService> hm = new HashMap<Class<?
                extends IFloodlightService>, IFloodlightService>();
        hm.put(IMACTrackerService.class, this);
        return hm;
    }
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
// TODO Auto-generated method stub
        Collection<Class<? extends IFloodlightService>> array = new ArrayList<Class<? extends
                IFloodlightService>>();
        array.add(IFloodlightProviderService.class);
        array.add(IRestApiService.class);
        return array;
    }
    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
// TODO Auto-generated method stub
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        macAddresses = new ConcurrentSkipListSet<Long>();
        logger = LoggerFactory.getLogger(MACTracker.class);
        restApi = context.getServiceImpl(IRestApiService.class);
    }
    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {

// TODO Auto-generated method stub
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
        restApi.addRestletRoutable(new MACTrackerWebRoutable());
    }
    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
// TODO Auto-generated method stub
        Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
                IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
//Para obtener la MAC
        Long sourceMACHash = eth.getSourceMACAddress().getLong();
        if (!macAddresses.contains(sourceMACHash)) {
//Para añadir a la lista
            macAddresses.add(sourceMACHash);
            logger.info("MAC Address: {} seen on switch: {}",
                    eth.getSourceMACAddress().toString(),
                    sw.getId().toString());
        }
        return Command.CONTINUE;
    }
    @Override
    public Set<Long> getMac() {
// TODO Auto-generated method stub
        return macAddresses;
    }
}

