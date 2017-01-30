package de.fhg.iais.jrdfb.serializer.example;
import de.fhg.iais.jrdfb.annotation.RdfProperty;
import de.fhg.iais.jrdfb.annotation.RdfType;
import de.fhg.iais.jrdfb.vocabulary.VCARD;

import java.util.List;

@RdfType(VCARD.INDIVIDUAL)
public class Person
{
    @RdfProperty(VCARD.FN)
    private String name;

    @RdfProperty(VCARD.HAS_ADDRESS)
	private Address address;

	private List<Person> friends;

    public Person() {
    }

    public Person(String name){
        this.name = name;
    }

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public List<Person> getFriends() {
		return friends;
	}

	public void setFriends(List<Person> friends) {
		this.friends = friends;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
