package bimserverclientdemo;


import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import org.bimserver.models.ifc2x3tc1.IfcAmountOfSubstanceMeasure;
import org.bimserver.models.ifc2x3tc1.IfcAreaMeasure;
import org.bimserver.models.ifc2x3tc1.IfcBoolean;
import org.bimserver.models.ifc2x3tc1.IfcComplexNumber;
import org.bimserver.models.ifc2x3tc1.IfcComplexProperty;
import org.bimserver.models.ifc2x3tc1.IfcContextDependentMeasure;
import org.bimserver.models.ifc2x3tc1.IfcCountMeasure;
import org.bimserver.models.ifc2x3tc1.IfcDescriptiveMeasure;
import org.bimserver.models.ifc2x3tc1.IfcElectricCurrentMeasure;
import org.bimserver.models.ifc2x3tc1.IfcIdentifier;
import org.bimserver.models.ifc2x3tc1.IfcInteger;
import org.bimserver.models.ifc2x3tc1.IfcLabel;
import org.bimserver.models.ifc2x3tc1.IfcLengthMeasure;
import org.bimserver.models.ifc2x3tc1.IfcLogical;
import org.bimserver.models.ifc2x3tc1.IfcLuminousIntensityMeasure;
import org.bimserver.models.ifc2x3tc1.IfcMassMeasure;
import org.bimserver.models.ifc2x3tc1.IfcMeasureValue;
import org.bimserver.models.ifc2x3tc1.IfcNormalisedRatioMeasure;
import org.bimserver.models.ifc2x3tc1.IfcNumericMeasure;
import org.bimserver.models.ifc2x3tc1.IfcObject;
import org.bimserver.models.ifc2x3tc1.IfcParameterValue;
import org.bimserver.models.ifc2x3tc1.IfcPlaneAngleMeasure;
import org.bimserver.models.ifc2x3tc1.IfcPositiveLengthMeasure;
import org.bimserver.models.ifc2x3tc1.IfcPositivePlaneAngleMeasure;
import org.bimserver.models.ifc2x3tc1.IfcPositiveRatioMeasure;
import org.bimserver.models.ifc2x3tc1.IfcProperty;
import org.bimserver.models.ifc2x3tc1.IfcPropertyEnumeratedValue;
import org.bimserver.models.ifc2x3tc1.IfcPropertySet;
import org.bimserver.models.ifc2x3tc1.IfcPropertySetDefinition;
import org.bimserver.models.ifc2x3tc1.IfcPropertySingleValue;
import org.bimserver.models.ifc2x3tc1.IfcRatioMeasure;
import org.bimserver.models.ifc2x3tc1.IfcReal;
import org.bimserver.models.ifc2x3tc1.IfcRelDefines;
import org.bimserver.models.ifc2x3tc1.IfcRelDefinesByProperties;
import org.bimserver.models.ifc2x3tc1.IfcSimpleProperty;
import org.bimserver.models.ifc2x3tc1.IfcSimpleValue;
import org.bimserver.models.ifc2x3tc1.IfcSolidAngleMeasure;
import org.bimserver.models.ifc2x3tc1.IfcText;
import org.bimserver.models.ifc2x3tc1.IfcThermodynamicTemperatureMeasure;
import org.bimserver.models.ifc2x3tc1.IfcTimeMeasure;
import org.bimserver.models.ifc2x3tc1.IfcValue;
import org.bimserver.models.ifc2x3tc1.IfcVolumeMeasure;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;


public class PropertyObject 
{
	private IfcObject object;
	HashMap<String, String> dimensions = new HashMap<String, String>();
	
	public PropertyObject(IfcObject object) throws ServerException, UserException
	{
		this.object = object;
		getPropertiesContainer();
	}
	
	private void getPropertiesContainer() throws ServerException, UserException 
	{
		List<IfcRelDefines> defs = object.getIsDefinedBy();
		
		for (IfcRelDefines def : defs)
		{
			if (def instanceof IfcRelDefinesByProperties )
			{
				IfcRelDefinesByProperties p = (IfcRelDefinesByProperties) def;
				IfcPropertySetDefinition psetdef = p.getRelatingPropertyDefinition();
				if (psetdef instanceof IfcPropertySet)
				{
					IfcPropertySet pset = (IfcPropertySet)psetdef;
					for (IfcProperty prop : pset.getHasProperties())
					{
						extractProperty(prop);
					}
				}
				
			}
		
		}
		
	}
	
	private void extractProperty(IfcProperty prop) throws ServerException
	{
		if (prop instanceof IfcComplexProperty)
		{
			IfcComplexProperty complex = (IfcComplexProperty)prop;
			for (IfcProperty p : complex.getPartOfComplex() )
			{
				extractProperty(p);
			}
		}
		else if (prop instanceof IfcSimpleProperty)
		{
			IfcSimpleProperty psimple = (IfcSimpleProperty)prop;
			
			
			if (psimple instanceof IfcPropertySingleValue)
			{
				IfcPropertySingleValue singval = (IfcPropertySingleValue)psimple;
				IfcValue nomval = singval.getNominalValue();
				dimensions.put(singval.getName(), extractValue(nomval));
			}
			else if (psimple instanceof IfcPropertyEnumeratedValue)
			{
				IfcPropertyEnumeratedValue sv = (IfcPropertyEnumeratedValue)psimple;
				
				for (IfcValue val : sv.getEnumerationValues())
				{
					dimensions.put(sv.getName(), extractValue(val));
				}
			} 
		}
		
		
			
	
	}
	
	public String extractValue(IfcValue nomval) throws ServerException
	{
		
		
		if (nomval instanceof IfcSimpleValue)
		{
			IfcSimpleValue sv = (IfcSimpleValue)nomval;
			
			if (sv instanceof IfcInteger)
				return Integer.toString((int) ((IfcInteger)sv).getWrappedValue());
			else if (sv instanceof IfcReal)
				return Double.toString(((IfcReal)sv).getWrappedValue());
			else if (sv instanceof IfcBoolean)
				return Boolean.toString(((IfcBoolean)sv).isSetWrappedValue());
			else if (sv instanceof IfcIdentifier)
				return ((IfcIdentifier)sv).getWrappedValue();
			else if (sv instanceof IfcText)
				return ((IfcText)sv).getWrappedValue();
			else if (sv instanceof IfcLabel)
				return ((IfcLabel)sv).getWrappedValue();
			else if (sv instanceof IfcLogical)
				return Boolean.toString(((IfcLogical)sv).isSetWrappedValue());
			
		}
		else if (nomval instanceof IfcMeasureValue)
		{
			IfcMeasureValue sv = (IfcMeasureValue)nomval;
		
			if (sv instanceof IfcVolumeMeasure)
				return new Double(((IfcVolumeMeasure)sv).getWrappedValue()).toString();
			else if (sv instanceof IfcAreaMeasure)
				return new Double(((IfcAreaMeasure)sv).getWrappedValue()).toString();
			else if (sv instanceof IfcTimeMeasure)
				return new Double(((IfcTimeMeasure)sv).getWrappedValue()).toString();
			else if (sv instanceof IfcThermodynamicTemperatureMeasure)
				return new Double(((IfcThermodynamicTemperatureMeasure)sv).getWrappedValue()).toString();
			else if (sv instanceof IfcSolidAngleMeasure)
				return new Double(((IfcSolidAngleMeasure)sv).getWrappedValue()).toString();
			else if (sv instanceof IfcPositiveRatioMeasure)
				return new Double(((IfcPositiveRatioMeasure)sv).getWrappedValue()).toString();
			else if (sv instanceof IfcRatioMeasure)
				return new Double(((IfcRatioMeasure)sv).getWrappedValue()).toString();
			else if (sv instanceof IfcPositivePlaneAngleMeasure)
				return new Double(((IfcPositivePlaneAngleMeasure)sv).getWrappedValue()).toString();
			else if (sv instanceof IfcPlaneAngleMeasure)
				return new Double(((IfcPlaneAngleMeasure)sv).getWrappedValue()).toString();
			else if (sv instanceof IfcParameterValue)
				return new Double(((IfcParameterValue)sv).getWrappedValue()).toString();
			else if (sv instanceof IfcNumericMeasure)
				return new Double(((IfcNumericMeasure)sv).getWrappedValue()).toString();
			else if (sv instanceof IfcMassMeasure)
				return new Double(((IfcMassMeasure)sv).getWrappedValue()).toString();
			else if (sv instanceof IfcPositiveLengthMeasure)
				return new Double(((IfcPositiveLengthMeasure)sv).getWrappedValue()).toString();
			else if (sv instanceof IfcLengthMeasure)
				return new Double(((IfcLengthMeasure)sv).getWrappedValue()).toString();
			else if (sv instanceof IfcElectricCurrentMeasure)
				return new Double(((IfcElectricCurrentMeasure)sv).getWrappedValue()).toString();
			else if (sv instanceof IfcDescriptiveMeasure)
				return ((IfcDescriptiveMeasure)sv).getWrappedValue();
			else if (sv instanceof IfcCountMeasure)
				return new Double(((IfcCountMeasure)sv).getWrappedValue()).toString();
			else if (sv instanceof IfcContextDependentMeasure)
				return new Double(((IfcContextDependentMeasure)sv).getWrappedValue()).toString();
			else if (sv instanceof IfcAmountOfSubstanceMeasure)
				return new Double(((IfcAmountOfSubstanceMeasure)sv).getWrappedValue()).toString();
			else if (sv instanceof IfcLuminousIntensityMeasure)
				return new Double(((IfcLuminousIntensityMeasure)sv).getWrappedValue()).toString();
			else if (sv instanceof IfcNormalisedRatioMeasure)
				return new Double(((IfcNormalisedRatioMeasure)sv).getWrappedValue()).toString();
			else if (sv instanceof IfcComplexNumber)
				return ((IfcComplexNumber)sv).getWrappedValueAsString().toString();
		}
		return "";
		
	}
	
	
	
	public void printProperties()
	{
		System.out.println();
		System.out.println();
		System.out.println("Properies for object");
		System.out.println("GUID: " + object.getGlobalId() 
				+ "  Type: " + object.getObjectType()
				+ "  Name: " + object.getName());
		System.out.println();
		
		Iterator<String> it = dimensions.keySet().iterator();
		while(it.hasNext())
		{
			String propertyName = it.next();
			System.out.println("Property: " + propertyName 
					+ "  Value: " + dimensions.get(propertyName));
		}
	}
	
	public void exportAllProperties(ExcelSheet sheet) throws RowsExceededException, WriteException 
	{
		Iterator<String> it = dimensions.keySet().iterator();
		int row = 0;
		while(it.hasNext())
		{
			row++;
			exportPropertyToExcel(1, row,  it.next(), true, sheet);
		}
	}
	
	
	
	public void exportPropertyToExcel( int column, int row, String propertyName, boolean label, ExcelSheet sheet) throws RowsExceededException, WriteException 
	{
		
		if (label == true)
		{
			Label labelCell = new Label(column, row, propertyName);
			sheet.getSheet().addCell(labelCell);
			column++;
		}
		
		String valueStr = dimensions.get(propertyName);
		
		try
		{
			Double value = Double.parseDouble(valueStr);
			Number number = new Number(column, row, value.doubleValue());
			sheet.getSheet().addCell(number);
		}
		catch (NumberFormatException ex)
		{
			// not a number add the verbatim string ...
			Label valueCell = new Label(column, row, valueStr);
			sheet.getSheet().addCell(valueCell);
		}
	}
	
	public String getQuantity(String quantityName)
	{
		if (dimensions.containsKey(quantityName))
			return dimensions.get(quantityName);
		else
			return null;
	}
	
 
	
	
	public static void main(String[] args) throws ServerException, UserException, RowsExceededException, BiffException, WriteException, IOException
	{
		
	}
}