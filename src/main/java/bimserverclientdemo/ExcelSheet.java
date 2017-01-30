package bimserverclientdemo;


import java.io.File;
import java.io.IOException;

import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExcelSheet 
{
	WritableSheet sheet = null;
	WritableWorkbook copy = null;
	
	public ExcelSheet(String fileIn, String fileOut, String sheetName) throws BiffException, IOException
	{
		Workbook workbook = Workbook.getWorkbook(new File(fileIn));
		copy = Workbook.createWorkbook(new File(fileOut), workbook);
		
		sheet = copy.getSheet(sheetName);
	}

	public WritableSheet getSheet() {
		return sheet;
	}

	public void setSheet(WritableSheet sheet) {
		this.sheet = sheet;
	}
	
	public void writeAndClose() throws IOException, WriteException
	{
		if (copy != null)
		{
			copy.write();
			copy.close();
		}
	}
}

