package edu.ucla.wise.persistence.data;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import edu.ucla.wise.persistence.invitee.Invitee;
import edu.ucla.wise.shared.persistence.GenericDAO;
import edu.ucla.wise.shared.persistence.HibernateUtil;


@Entity
@Table(schema = TextAnswer.SCHEMA_NAME, name = TextAnswer.TABLE_NAME)
public class TextAnswer {

	public static final String SCHEMA_NAME = "wisedev";
	public static final String TABLE_NAME = "main_data_text";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private int id;
	
	@Column(name = "surveyId", nullable = false)
	private int survey;
	
	@Column(name = "inviteeId", nullable = false)
	private int invitee;
	
	@Column(name = "questionId", nullable = false)
	private String question;
	
	@Column(name = "answer", nullable = false)
	private String answer;
	
	
	public TextAnswer(int surveyId, int inviteeId, String questionId, String answer){
		this.survey = surveyId;
		this.invitee = inviteeId;
		this.question = questionId;
		this.answer = answer;
	}
	
	protected TextAnswer(){
		//for hibernate
	}
	
	public int getId() {
		return id;
	}



	public void setId(int id) {
		this.id = id;
	}



	public int getSurvey() {
		return survey;
	}



	public void setSurvey(int survey) {
		this.survey = survey;
	}



	public int getInvitee() {
		return invitee;
	}



	public void setInvitee(int invitee) {
		this.invitee = invitee;
	}



	public String getQuestion() {
		return question;
	}



	public void setQuestion(String question) {
		this.question = question;
	}



	public String getAnswer() {
		return answer;
	}



	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public static TextAnswer getEmptyExample(){
		return new TextAnswer();
	}
	
	public static class TextAnswerDAO extends GenericDAO<TextAnswer,Integer>{

		public TextAnswerDAO(HibernateUtil hibernateUtil) {
			super(hibernateUtil);
		}
	}
	
	
}
