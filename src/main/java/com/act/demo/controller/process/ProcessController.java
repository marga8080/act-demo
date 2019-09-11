package com.act.demo.controller.process;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.image.ProcessDiagramGenerator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.act.demo.config.ResponseResult;
import com.act.demo.controller.BaseController;

@RequestMapping("process")
@Controller
public class ProcessController extends BaseController {

	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private ProcessEngineConfigurationImpl processEngineConfiguration;

	/**
	 * 启动流程
	 * 
	 * @param deploymentId 部署id
	 * @return
	 */
	@GetMapping("start")
	@ResponseBody
	public ResponseResult startProcess(String deploymentId) {
		ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).singleResult();
		ProcessInstance pi = runtimeService.startProcessInstanceById(pd.getId());
		logger.info("processInstance = {}, process'key = {}, process'name = {}", pi, pi.getProcessDefinitionKey(), pi.getName());
		return success("启动成功", null);
	}
	
	/**
	 * 流程流转
	 * @param taskId
	 * @return
	 */
	@GetMapping("next")
	@ResponseBody
	public ResponseResult nextTask(String taskId) {
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
		taskService.complete(taskId, variables);
		return success("处理成功", null);
	}
	
	/**
	 * 结束流程
	 * @return
	 */
	@GetMapping("complete")
	@ResponseBody
	public ResponseResult completeTask(String taskId) {
		taskService.complete(taskId);
		return success("结束流程成功", null);
	}

	/**
	 * 任务列表
	 * @param assignee 受理人
	 * @param model
	 * @return
	 */
	@GetMapping("taskList")
	public String taskList(String assignee, Model model) {
		TaskQuery query = taskService.createTaskQuery().orderByTaskCreateTime().desc();
		if (StringUtils.isNotBlank(assignee)) {
			query.taskAssignee(assignee);
		}
		List<Task> tasks = query.list();
		model.addAttribute("tasks", tasks);
		return "task/list";
	}
	
	

	/**
	 * 获取流程图
	 * @param taskId
	 * @param response
	 * @throws IOException
	 */
	@GetMapping("getProcessImg")
	public void getProcessImg(String taskId, HttpServletResponse response) throws IOException {
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		String processInstanceId = task.getProcessInstanceId();
		// 获取历史流程实例
		HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		// 获取流程图
		BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());

		ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
		ProcessDefinitionEntity definitionEntity = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processInstance.getProcessDefinitionId());

		List<HistoricActivityInstance> highLightedActivitList = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).list();
		// 高亮环节id集合
		List<String> highLightedActivitis = new ArrayList<String>();

		// 高亮线路id集合
		List<String> highLightedFlows = getHighLightedFlows(definitionEntity, highLightedActivitList);
		for (HistoricActivityInstance tempActivity : highLightedActivitList) {
			String activityId = tempActivity.getActivityId();
			highLightedActivitis.add(activityId);
		}
		// 配置字体
		InputStream imageStream = diagramGenerator.generateDiagram(bpmnModel, "png", highLightedActivitis, highLightedFlows, "宋体", "微软雅黑", "黑体", null, 2.0);
		BufferedImage bi = ImageIO.read(imageStream);
		OutputStream out = response.getOutputStream();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(bi, "png", bos);
		byte[] bytes = bos.toByteArray();// 转换成字节
		IOUtils.write(bytes, out);
	}

	/**
	 * 获取需要高亮的线
	 * 
	 * @param processDefinitionEntity
	 * @param historicActivityInstances
	 * @return
	 */
	private List<String> getHighLightedFlows(ProcessDefinitionEntity processDefinitionEntity, List<HistoricActivityInstance> historicActivityInstances) {
		List<String> highFlows = new ArrayList<String>();// 用以保存高亮的线flowId
		for (int i = 0; i < historicActivityInstances.size() - 1; i++) {// 对历史流程节点进行遍历
			ActivityImpl activityImpl = processDefinitionEntity.findActivity(historicActivityInstances.get(i).getActivityId());// 得到节点定义的详细信息
			List<ActivityImpl> sameStartTimeNodes = new ArrayList<ActivityImpl>();// 用以保存后需开始时间相同的节点
			ActivityImpl sameActivityImpl1 = processDefinitionEntity.findActivity(historicActivityInstances.get(i + 1).getActivityId());
			// 将后面第一个节点放在时间相同节点的集合里
			sameStartTimeNodes.add(sameActivityImpl1);
			for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
				HistoricActivityInstance activityImpl1 = historicActivityInstances.get(j);// 后续第一个节点
				HistoricActivityInstance activityImpl2 = historicActivityInstances.get(j + 1);// 后续第二个节点
				if (activityImpl1.getStartTime().equals(activityImpl2.getStartTime())) {
					// 如果第一个节点和第二个节点开始时间相同保存
					ActivityImpl sameActivityImpl2 = processDefinitionEntity.findActivity(activityImpl2.getActivityId());
					sameStartTimeNodes.add(sameActivityImpl2);
				} else {
					// 有不相同跳出循环
					break;
				}
			}
			List<PvmTransition> pvmTransitions = activityImpl.getOutgoingTransitions();// 取出节点的所有出去的线
			for (PvmTransition pvmTransition : pvmTransitions) {
				// 对所有的线进行遍历
				ActivityImpl pvmActivityImpl = (ActivityImpl) pvmTransition.getDestination();
				// 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
				if (sameStartTimeNodes.contains(pvmActivityImpl)) {
					highFlows.add(pvmTransition.getId());
				}
			}
		}
		return highFlows;
	}
}
