package com.act.demo.controller.models;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.act.demo.config.ResponseResult;
import com.act.demo.controller.BaseController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * https://www.jianshu.com/p/41f11c99167a?tdsourcetag=s_pcqq_aiomsg
 * https://www.cnblogs.com/zhouyun-yx/p/10410274.html
 * 
 * @author Administrator
 *
 */
@Controller
@RequestMapping("models")
public class ModelController extends BaseController {

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private ObjectMapper objectMapper;

	@GetMapping("list")
	public String modelList(org.springframework.ui.Model model, HttpServletRequest request) {
		List<Model> models = repositoryService.createModelQuery().orderByCreateTime().desc().list();
		model.addAttribute("models", models);
		String info = request.getParameter("info");
		if (StringUtils.isNotEmpty(info)) {
			model.addAttribute("info", info);
		}
		return "model/list";
	}

	@RequestMapping("create")
	public void newModel(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		try {
			// 初始化一个空模型
			Model model = repositoryService.newModel();
			// 设置一些默认信息
			String name = "新建流程";
			String description = "";
			int revision = 1;
			String key = "process";

			ObjectNode modelNode = objectMapper.createObjectNode();
			modelNode.put(ModelDataJsonConstants.MODEL_NAME, name);
			modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
			modelNode.put(ModelDataJsonConstants.MODEL_REVISION, revision);

			model.setName(name);
			model.setKey(key);
			model.setMetaInfo(modelNode.toString());

			repositoryService.saveModel(model);
			String id = model.getId();

			// 完善ModelEditorSource
			ObjectNode editorNode = objectMapper.createObjectNode();
			editorNode.put("id", "canvas");
			editorNode.put("resourceId", "canvas");
			ObjectNode stencilSetNode = objectMapper.createObjectNode();
			stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
			editorNode.set("stencilset", stencilSetNode);
			repositoryService.addModelEditorSource(id, editorNode.toString().getBytes("utf-8"));

			response.sendRedirect(request.getContextPath() + "/static/modeler.html?modelId=" + id);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	@GetMapping("delete")
	@ResponseBody
	public ResponseResult deleteModel(String id) {
		repositoryService.deleteModel(id);
		return success("删除成功", null);
	}

	/**
	 * 发布
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@GetMapping("deployment")
	@ResponseBody
	public ResponseResult deploy(String id) throws Exception {
		// 获取模型
		Model modelData = repositoryService.getModel(id);
		byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());
		if (bytes == null) {
			return error("模型数据为空，请先设计流程并成功保存，再进行发布。");
		}
		JsonNode modelNode = new ObjectMapper().readTree(bytes);

		BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
		if (model.getProcesses().size() == 0) {
			return error("数据模型不符要求，请至少设计一条主线流程。");
		}
		byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

		// 发布流程
		String processName = modelData.getName() + ".bpmn20.xml";
		Deployment deployment = repositoryService.createDeployment().name(modelData.getName()).addString(processName, new String(bpmnBytes, "UTF-8")).deploy();
		modelData.setDeploymentId(deployment.getId());
		repositoryService.saveModel(modelData);
		return success("流程发布成功", null);
	}

	@GetMapping("start")
	@ResponseBody
	public ResponseResult startProcess(String id) {
		try {

		} catch (Exception e) {
			logger.error(e);
			return error("流程启动失败！");
		}
		return success("流程启动成功", null);
	}
	
	/** 
     * 导出model的xml文件 
     */  
    @GetMapping("export")  
    public void export(String modelId, HttpServletResponse response) {  
        try {  
            Model modelData = repositoryService.getModel(modelId);  
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();  
            JsonNode editorNode = new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));  
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);  
            BpmnXMLConverter xmlConverter = new BpmnXMLConverter();  
            byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel);  
  
            ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);  
            IOUtils.copy(in, response.getOutputStream());  
            String filename = modelData.getName() + ".bpmn20.xml";  
            filename = new String(filename.getBytes("utf-8"), "iso-8859-1");
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);  
            response.flushBuffer();  
        } catch (Exception e) {  
        	logger.error("导出model的xml文件失败：modelId="+modelId, e);  
        }  
    } 
}
