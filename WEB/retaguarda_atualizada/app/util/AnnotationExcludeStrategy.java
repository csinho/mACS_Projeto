package util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;


public class AnnotationExcludeStrategy implements ExclusionStrategy {
	@Override
	public boolean shouldSkipClass(Class<?> clazz) {
		return false;
	}

	@Override
	public boolean shouldSkipField(FieldAttributes f) {
		return f.getAnnotation(Exclude.class) != null;
	}
 }