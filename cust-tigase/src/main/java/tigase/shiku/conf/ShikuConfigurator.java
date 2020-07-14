package tigase.shiku.conf;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import tigase.component.modules.Module;
import tigase.conf.Configurator;
import tigase.muc.MUCComponent;
import tigase.muc.modules.PresenceModule;
import tigase.muc.modules.PresenceModuleImpl;
import tigase.shiku.SKPresenceModule;
import tigase.shiku.ShikuPresenceModuleImpl;

public class ShikuConfigurator extends Configurator{
	
	
	@Override
	public void initializationCompleted() {
		super.initializationCompleted();
		if(ShikuConfigBean.ISOPENAUTOJOINROOM){
			System.out.println(this.getClass().getName()+"    initializationCompleted");
			MUCComponent mucComponent;
			try {
				mucComponent = (MUCComponent) components.get("muc");
				Module module = mucComponent.getModuleProvider().getModule(PresenceModule.ID);
				if(module instanceof SKPresenceModule){
					ShikuConfigBean.setPresenceModule((SKPresenceModule) module);
				}
				ShikuConfigBean.setMucComponent(mucComponent);
			  System.out.println("init presences class "+module.getClass().getName() +" muc Component class "+mucComponent.getClass().getName());
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}
		
	@Override
	public void parseArgs(String[] args) {
		// TODO Auto-generated method stub
		super.parseArgs(args);
		System.setProperty("rocketmq.client.logLevel", "WARN");
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory(); 
		Logger rootLogger = loggerContext.getLogger("org.mongodb.driver"); 
		rootLogger.setLevel(ch.qos.logback.classic.Level.ERROR); 
		Logger redisLogger = loggerContext.getLogger("org.redisson"); 
		redisLogger.setLevel(ch.qos.logback.classic.Level.ERROR); 
		System.out.println("============ShikuConfigurator  > parseArgs ");
		
	}
	
	@Override
	public String getMessageRouterClassName() {
		// TODO Auto-generated method stub
		//return super.getMessageRouterClassName();
		
		return "tigase.shiku.conf.ShikuMessageRouter";
	}
}
