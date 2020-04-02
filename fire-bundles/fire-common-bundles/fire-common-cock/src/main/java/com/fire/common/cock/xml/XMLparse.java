package com.fire.common.cock.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.Reader;

public class XMLparse {

	public static <T> T xmlToInstance(Class<T> c, Reader reader) {
		try {
			JAXBContext context = JAXBContext.newInstance(c);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			@SuppressWarnings("unchecked")
			T t = (T) unmarshaller.unmarshal(reader);
			return t;
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return null;
	}
}
