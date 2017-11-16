package ru.gifttogift.db;

import ru.gifttogift.DataGift;
import ru.gifttogift.Main;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GiftDB {
    private Connection conn;
    private String[] TABLE_NAME;

    // TODO изменить на пользовательский ввод логина и пароля для БД
    public GiftDB() throws ClassNotFoundException, SQLException {
        TABLE_NAME = new String[]{"new_schema.GIFTS", "new_schema.IMAGES"};
        String name = "admin";
        String password = "Anton4447138";

        String url = "jdbc:sqlserver://192.168.1.13:49389;database=GiftToGift";

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        conn = DriverManager.getConnection(url, name, password);
    }

    public GiftDB(String name, String password) throws ClassNotFoundException, SQLException {
        TABLE_NAME = new String[]{"new_schema.GIFTS", "new_schema.IMAGES"};

        String url = "jdbc:sqlserver://192.168.1.13:49389;database=GiftToGift";

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        conn = DriverManager.getConnection(url, name, password);
    }

    /*
    INSERT INTO [dbo].[GIFTS]
           ([NAME]
           ,[DESCRIBE]
           ,[CATEGORY]
           ,[COST]
           ,[CODE])
     VALUES
           (<NAME, varchar(max),>
           ,<DESCRIBE, varchar(max),>
           ,<CATEGORY, varchar(50),>
           ,<COST, float,>
           ,<CODE, varchar(10),>)
     */

    public boolean addGift(DataGift dataGift){
        //System.out.println(dataGift);
        if(!containsCurrGift(dataGift)) {
            try {
                String name = dataGift.getName();
                if(name.contains("'")){
                    name = name.replace("'", "''");
                }
                PreparedStatement statement = conn.prepareStatement("INSERT " + TABLE_NAME[0] + " VALUES('" +
                        name + "', '" +
                        dataGift.getDescription() + "', '" +
                        dataGift.getCategory() + "', '" +
                        dataGift.getUrl() + "', " +
                        dataGift.getPrice() + ", '" +
                        dataGift.getCode() + "')", Statement.RETURN_GENERATED_KEYS);
                int affectedRows = statement.executeUpdate();
                //System.out.println(affectedRows);
                ResultSet resultSet = statement.getGeneratedKeys();
                int id = -1;
                if (resultSet.next())
                    id = resultSet.getInt(1);
                ArrayList<String> list = dataGift.getImages();
                if (id != -1)
                    for (String path : list) {
                        if(!containsImage(id, path)) {
                            statement = conn.prepareStatement("INSERT " + TABLE_NAME[1] + " VALUES(" +
                                    id + ", '" +
                                    path + "')");
                            statement.executeUpdate();
                        }
                    }
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                Main.logFile.write(e);
                //Main.logFile.writeInfo(dataGift.getUrl());
                Main.logFile.writeInfo(dataGift.toString());
            }
        }
        System.out.println("exist");
        return false;
    }

    public boolean containsCurrGift(DataGift dataGift){
        try{
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT NAME, DESCRIPTION, CATEGORY, CODE FROM " + TABLE_NAME[0] + " WHERE CODE = '" + dataGift.getCode() + "'");
            while(resultSet.next()){
                String name = resultSet.getString(1);
                String description = resultSet.getString(2);
                String category = resultSet.getString(3);
                //System.out.println("name: " + name + " vs " + dataGift.getName() + " resutl: " + dataGift.getName().equalsIgnoreCase(name));
                //System.out.println("category: " + category + " vs " + dataGift.getCategory() + " result: " + dataGift.getCategory().equals(category));
                //System.out.println("description: " + description + " vs " + dataGift.getDescription() + "result: " + dataGift.getDescription().equals(description));
                if(dataGift.getName().equalsIgnoreCase(name) && dataGift.getCategory().equals(category) && dataGift.getDescription().equals(description)){
                    //System.err.println("I FOUND GIFT!!! " + dataGift.getUrl());
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean containsImage(int id, String path){
        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT ID, IDGIFT, PATH FROM " + TABLE_NAME[1] + " WHERE IDGIFT = " + id + "");
            while (resultSet.next()) {
                String pathGift = resultSet.getString(3);
                if (path.equalsIgnoreCase(pathGift)) {
                    System.err.println("I FOUND IMAGE!!! " + pathGift);
                    return true;
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean runQuery(String query) throws SQLException {
        Statement statement = conn.createStatement();

        return true;
    }


    //TODO улучшить выборку нужных элеметов из БД
    public List<DataGift> getGifts(int index, int count){
        List<DataGift> list = new ArrayList<>();
        List<Integer> id = new ArrayList<>();
        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT TOP(" + count + ") ID, NAME, DESCRIPTION, CATEGORY, URL, COST, CODE FROM " + TABLE_NAME[0] +
                    " WHERE ID > " + index + " ORDER BY ID");
            while(resultSet.next()){
                //System.out.println(resultSet.getFetchSize());
                DataGift gift = new DataGift(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4),
                        resultSet.getString(5),
                        resultSet.getFloat(6), resultSet.getString(7));
                id.add(resultSet.getInt(1));
                /*ResultSet resultSetImage = statement.executeQuery("SELECT ID, ID_GIFTS, PATH FROM " + TABLE_NAME[1] + " WHERE ID_GIFTS = " + resultSet.getInt(1));
                while(resultSetImage.next()){
                    gift.AddImagePath(resultSetImage.getString(3));
                }*/
                list.add(gift);
            }
            statement = conn.createStatement();
            //System.err.println(id.size());
            for(int i = 0; i < id.size(); i++){
                ResultSet resultSetImage = statement.executeQuery("SELECT ID, IDGIFT, PATH FROM " + TABLE_NAME[1] + " WHERE IDGIFT = " + id.get(i));
                while(resultSetImage.next()){
                    list.get(i).AddImagePath(resultSetImage.getString(3));
                }
            }
        } catch (SQLException e) {
            Main.logFile.write(e);
        }
        return list;
    }
}
