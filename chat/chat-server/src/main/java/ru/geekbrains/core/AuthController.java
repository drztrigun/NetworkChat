package ru.geekbrains.core;

import ru.geekbrains.data.User;

import java.util.ArrayList;
import java.util.HashMap;

public class AuthController {

    HashMap<String, User> users = new HashMap<>();

    public void init(){
        for (User user:receiveUsers()) {
            users.put(user.getLogin(), user);
        }
    }

    public String getNickname (String login, String password){
        User user = users.get(login);
        if(user != null && user.isPasswordCorrect(password)){
            return user.getNickname();
        }
        return null;
    }

    private ArrayList<User> receiveUsers() {
        ArrayList<User> userArr = new ArrayList<>();
        userArr.add(new User("admin", "admin", "sysroot"));
        userArr.add(new User("user1", "user1", "user1- st"));
        return userArr;
    }
}
