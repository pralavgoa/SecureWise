package edu.ucla.wise.persistence.data;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

public class UserData {

	private final int userId;

	private final Multimap<String, String> textAnswers;
	private final Multimap<String,Integer> numericAnswers;

	public UserData(int userId){
		this.userId = userId;
		textAnswers = ArrayListMultimap.create();
		numericAnswers = ArrayListMultimap.create();
	}

	public void addAnswer(String questionId, String answer){
		textAnswers.put(questionId, answer);
	}

	public void addAnswer(String questionId, int answer){
		numericAnswers.put(questionId, answer);
	}

	public String getTextAnswer(String questionId){
		return Iterables.getLast(textAnswers.get(questionId));
	}
	
	public int getNumericAnswer(String questionId){
		return Iterables.getLast(numericAnswers.get(questionId));
	}

	public int getUserId() {
		return userId;
	}
	
	

}
