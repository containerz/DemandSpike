package com.neverwinterdp.server.gateway;

import com.beust.jcommander.Parameter;
import com.neverwinterdp.demandspike.DemandSpikeJobSchedulerInfo;
import com.neverwinterdp.demandspike.job.DemandSpikeJobService;
import com.neverwinterdp.demandspike.job.config.DemandSpikeJob;
import com.neverwinterdp.server.cluster.ClusterClient;
import com.neverwinterdp.server.command.ServiceCommand;
import com.neverwinterdp.server.command.ServiceCommands;
import com.neverwinterdp.util.IOUtil;
import com.neverwinterdp.util.JSONSerializer;

@CommandPluginConfig(name = "demandspike")
public class DemandSpikeCommandPlugin extends CommandPlugin {
  public DemandSpikeCommandPlugin() {
    add("submit", new submit()) ;
    add("scheduler", new scheduler()) ;
  }
  
  static public class submit implements SubCommandExecutor {
    @Parameter(names = {"-f", "--file"}, required = true , description = "The DemandSpikeJob in a json file")
    private String file ;
    
    
    public Object execute(ClusterClient clusterClient, Command command) throws Exception {
      command.mapAll(this);
      
      String json = IOUtil.getFileContentAsString(file) ;
      DemandSpikeJob job = JSONSerializer.INSTANCE.fromString(json, DemandSpikeJob.class) ;
      
      ServiceCommand<Boolean> methodCall = 
          new ServiceCommands.MethodCall<Boolean>("submit", job, command.getMemberSelector().timeout) ;
      methodCall.setTargetService("DemandSpike", DemandSpikeJobService.class.getSimpleName());
      return  command.getMemberSelector().execute(clusterClient, methodCall) ;
    }
  }
 
  static public class scheduler implements SubCommandExecutor {
    public Object execute(ClusterClient clusterClient, Command command) throws Exception {
      ServiceCommand<DemandSpikeJobSchedulerInfo> methodCall = 
          new ServiceCommands.MethodCall<DemandSpikeJobSchedulerInfo>("getSchedulerInfo") ;
      methodCall.setTargetService("DemandSpike", DemandSpikeJobService.class.getSimpleName());
      return command.getMemberSelector().execute(clusterClient, methodCall) ;
    }
  }
}