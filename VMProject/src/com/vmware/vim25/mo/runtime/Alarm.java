package com.vmware.vim25.mo.runtime;

import java.rmi.RemoteException;

import com.vmware.vim25.Action;
import com.vmware.vim25.AlarmAction;
import com.vmware.vim25.AlarmSetting;
import com.vmware.vim25.AlarmSpec;
import com.vmware.vim25.AlarmState;
import com.vmware.vim25.AlarmTriggeringAction;
import com.vmware.vim25.GroupAlarmAction;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.MethodAction;
import com.vmware.vim25.MethodActionArgument;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.SendEmailAction;
import com.vmware.vim25.StateAlarmExpression;
import com.vmware.vim25.StateAlarmOperator;
import com.vmware.vim25.mo.AlarmManager;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.connect.Connect;

public class Alarm {
	
	private String vmname;
	public Alarm(String vmname){
		this.vmname=vmname;
	}
	
public static String getAlarmStatus(){
	
	Connect c = new Connect();
	ServiceInstance si = c.getServiceInstance();
	AlarmManager alMgr = si.getAlarmManager();
	ManagedEntity rootFolder = si.getRootFolder();
	VirtualMachine vm;
	String AlarmStatus;
	try {
		vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", "TEAM14_Ubuntu09");
		AlarmState[] aState = alMgr.getAlarmState(vm);

		for (AlarmState alarmState : aState) {
			if(alarmState.key.equals("alarm-108.vm-169") && alarmState.overallStatus.name().equals("red")){
				System.out.println("Alarm triggered-user has switched off VM");
				return AlarmStatus="Triggered";
			 }	 
			
		}
		}
	catch (RemoteException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return AlarmStatus="NotTriggered";	
}
		
public  void createAlarm() throws Exception{
	ServiceInstance si = Connect.getServiceInstance();
	Folder rootFolder = si.getRootFolder();
	
	VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmname);

	AlarmManager alarmMgr = si.getAlarmManager();
	AlarmSpec spec = new AlarmSpec();
	
	StateAlarmExpression expression = createStateAlarmExpression();
	AlarmAction methodAction = createAlarmTriggerAction(createPowerOnAction());
	GroupAlarmAction gaa = new GroupAlarmAction();

	gaa.setAction(new AlarmAction[]{ methodAction});
	spec.setAction(gaa);
	spec.setExpression(expression);
	spec.setName("VmPowerStateAlarm-createdByGaurav");
	spec.setDescription("Monitor VM state and power it on if VM powers off");
	spec.setEnabled(true);    

	AlarmSetting as = new AlarmSetting();
	as.setReportingFrequency(0); //as often as possible
	as.setToleranceRange(0);

	spec.setSetting(as);
	alarmMgr.createAlarm(vm, spec);
	si.getServerConnection().logout();
}

static StateAlarmExpression createStateAlarmExpression()
{
	StateAlarmExpression expression = 
			new StateAlarmExpression();
	expression.setType("VirtualMachine");
	expression.setStatePath("runtime.powerState");
	expression.setOperator(StateAlarmOperator.isEqual);
	expression.setRed("poweredOff");
	return expression;
}

static MethodAction createPowerOnAction() 
{
	MethodAction action = new MethodAction();
	action.setName("PowerOnVM_Task");
	MethodActionArgument argument = new MethodActionArgument();
	argument.setValue(null);
	action.setArgument(new MethodActionArgument[] { argument });
	return action;
}

static AlarmTriggeringAction createAlarmTriggerAction(Action action) 
{
	AlarmTriggeringAction alarmAction =  new AlarmTriggeringAction();
	alarmAction.setYellow2red(true);
	alarmAction.setAction(action);
	return alarmAction;
}
}
