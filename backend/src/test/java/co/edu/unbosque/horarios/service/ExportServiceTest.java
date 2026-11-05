package co.edu.unbosque.horarios.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import co.edu.unbosque.horarios.api.ApiDtos;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExportServiceTest {
  private final ScheduleService schedules=mock(ScheduleService.class);
  private final ExportService exports=new ExportService(schedules);

  @Test @DisplayName("CP-39: exporta el horario como un PDF válido")
  void cp39_exportaPdf(){
    when(schedules.get(1L)).thenReturn(view());
    var bytes=exports.pdf(1L);
    assertTrue(bytes.length>500);
    assertEquals("%PDF",new String(bytes,0,4,StandardCharsets.US_ASCII));
  }

  @Test @DisplayName("CP-40: exporta el horario a Excel con encabezados y sesiones")
  void cp40_exportaExcel() throws Exception {
    when(schedules.get(1L)).thenReturn(view());
    var bytes=exports.xlsx(1L);
    try(var book=new XSSFWorkbook(new ByteArrayInputStream(bytes))){
      var sheet=book.getSheet("Horario");
      assertNotNull(sheet);
      assertEquals(2,sheet.getPhysicalNumberOfRows());
      assertEquals("Código",sheet.getRow(0).getCell(0).getStringCellValue());
      assertEquals("SIS-301",sheet.getRow(1).getCell(0).getStringCellValue());
      assertEquals("LAB-3",sheet.getRow(1).getCell(4).getStringCellValue());
    }
  }

  private ApiDtos.ScheduleView view(){
    var session=new ApiDtos.SessionView(1L,1L,"SIS-301","Bases de Datos",1,
        "Laura Méndez","LAB-3",1,"07:00","09:00");
    return new ApiDtos.ScheduleView(1L,"2026-2",1,"PUBLICADO",List.of(session),List.of());
  }
}
