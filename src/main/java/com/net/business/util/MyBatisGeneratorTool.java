package com.net.business.util;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MyBatisGeneratorTool {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		 	List<String> warnings = new ArrayList<String>();  
	        ConfigurationParser cp = new ConfigurationParser(warnings);  
	  
	        boolean overwrite = true;  
	        //staticTableConfig.xml,dynamicTableConfig.xml 
	        File configFile = new File("src/main/resources/generatorConfig.xml");
	        try {  
	            Configuration config = cp.parseConfiguration(configFile);  
	            DefaultShellCallback callback = new DefaultShellCallback(overwrite);  
	            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);  
	            myBatisGenerator.generate(null);  
	            System.out.println("完成！");
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
	}

}