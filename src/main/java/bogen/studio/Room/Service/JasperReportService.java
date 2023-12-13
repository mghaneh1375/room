package bogen.studio.Room.Service;

import bogen.studio.Room.Models.PassengerInfo;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Service
@RequiredArgsConstructor
public class JasperReportService {

    public void buildAndExportVoucher(HttpServletResponse httpResponse) throws IOException, JRException {

        File file = ResourceUtils.getFile("classpath:Blank_A4.jrxml");

        JasperDesign jasperDesign = JRXmlLoader.load(file.getAbsolutePath());

        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
        //JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());

        // Create data source ...
        List<PassengerInfo> passengerInfos = new ArrayList<>();
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(passengerInfos);

        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("CREATOR_FIRST_NAME", "رضا");
        parameterMap.put("CREATOR_LAST_NAME", "آزاد");



        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameterMap, new JREmptyDataSource());
        JasperExportManager.exportReportToPdfFile(jasperPrint, "r1.pdf");

        JasperExportManager.exportReportToPdfStream(jasperPrint, httpResponse.getOutputStream());



    }

}
