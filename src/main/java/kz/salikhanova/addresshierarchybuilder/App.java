package kz.salikhanova.addresshierarchybuilder;

import java.io.IOException;
import com.mashape.unirest.http.exceptions.UnirestException;

import kz.salikhanova.addresshierarchybuilder.util.AddressHierarchyBuilder;

/**
 * @author Saltanat Alikhanova
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	AddressHierarchyBuilder builder = new AddressHierarchyBuilder();
    	try {
			builder.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnirestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
