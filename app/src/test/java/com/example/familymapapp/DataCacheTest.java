package com.example.familymapapp;
import com.example.familymapapp.cache.DataCache;

import static org.junit.Assert.assertNotNull;


import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import Model.Person;
import com.example.familymapapp.UserInterface.LoginFragment;

public class DataCacheTest extends LoginFragment {
    private final Set<Person> fatherSideMales = new HashSet<>();
    private final Set<Person> fatherSideFemales = new HashSet<>();
    private final Set<Person> motherSideMales = new HashSet<>();
    private final Set<Person> motherSideFemales = new HashSet<>();

    public DataCacheTest(Listener listener) {
        super(listener);
    }

    @Before
    public void correctData() {
        Person person = new Person("Blaine_McGary", "sheila", "Blaine", "McGary", "m", "Ken_Rodham", "Mrs_Rodham", "Betty_White");
        //do this for everyone
        fatherSideMales.add(person);
        //later compare them all
    }
/*
    @Before
    public void loginSetUp() {
        View view = getView();
        Button loginButton = view.findViewById(R.id.LoginButton);
        IDEK how you would log in
    }
 */
    @Test
    public void dataCached () {
        DataCache dataCache = DataCache.getInstance();
        assertNotNull(dataCache);

        Set<Person> fatherSideFemales = dataCache.getFatherSideFemales();
        assertNotNull(fatherSideFemales);
    }
}