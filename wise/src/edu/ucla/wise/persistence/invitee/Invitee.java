package edu.ucla.wise.persistence.invitee;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import edu.ucla.wise.shared.persistence.GenericDAO;
import edu.ucla.wise.shared.persistence.HibernateUtil;

@Entity
@Table(schema = Invitee.SCHEMA_NAME, name = Invitee.TABLE_NAME)
public class Invitee {

	public static final String SCHEMA_NAME = "wisedev";
	public static final String TABLE_NAME = "invitee";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private int id;

	@Column(name = "firstname", nullable = false)
	private String firstName;
	
	@Column(name = "lastname", nullable = false)
	private String lastname;
	
	@Column(name = "salutation", nullable = false)
	private String salutation;
	
	@Column(name = "email", nullable = false)
	private String email;
	
	@Column(name = "phone")
	private String phone;
	
	@Column(name = "firstName")
	private String subjectType;
	
	protected Invitee(){
		//for hibernate
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getSalutation() {
		return salutation;
	}

	public void setSalutation(String salutation) {
		this.salutation = salutation;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getSubjectType() {
		return subjectType;
	}

	public void setSubjectType(String subjectType) {
		this.subjectType = subjectType;
	}
	
	public static class InviteeDAO extends GenericDAO<Invitee,Integer>{

		public InviteeDAO(HibernateUtil hibernateUtil) {
			super(hibernateUtil);
		}
		
	}
	
}



