package ru.geekbrains.data;

public class User {

    private String login;
    private String password;
    private String nickname;

    public User(String login, String password, String nickname){
        this.login = login;
        this.password = password;
        this.nickname = nickname;
    }

    public String getLogin() {
        return login;
    }

    public String getNickname() {
        return nickname;
    }

    public boolean isPasswordCorrect(String password){
        return  this.password.equals(password);
    }
}
