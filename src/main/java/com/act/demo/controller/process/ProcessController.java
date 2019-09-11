package com.act.demo.controller.process;

import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.act.demo.service.ActivitiService;

@RequestMapping("process")
@Controller
public class ProcessController {
	
	@Autowired
    private ActivitiService activitiServiceImpl;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;

//	/**
//	 * 启动流程
//	 * @param key
//	 * @param modelMap
//	 * @return
//	 */
//    @RequestMapping("/start/{key}")
//    public String startProcess(@PathVariable("key") String key, RedirectAttributesModelMap modelMap) {
//        ActivitiVariable activitiVariable = new ActivitiVariable();
//        int resul= activitiVariableService.insert(activitiVariable);
//        if(resul==1){
//            modelMap.addFlashAttribute("info","流程启动成功！");
//            String business_key = key+":"+activitiVariable.getId();
//            activitiServiceImpl.startProcesses(key,business_key);
//        }else{
//            modelMap.addFlashAttribute("info","流程启动失败！");
//        }
//        return "process/list";
//    }
}
