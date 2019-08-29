package com.automationintesting.db;

import com.automationintesting.model.Room;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.Server;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class RoomDB {

    private Connection connection;
    private final String SELECT_ROOMS = "SELECT * FROM PUBLIC.ROOMS";
    private final String SELECT_BY_ROOMID = "SELECT * FROM PUBLIC.ROOMS WHERE roomid = ?";
    private final String DELETE_BY_ROOMID = "DELETE FROM PUBLIC.ROOMS WHERE roomid = ?";
    private final String DELETE_ALL_ROOMS = "DELETE FROM PUBLIC.ROOMS";

    public RoomDB() throws SQLException {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:rbp");
        ds.setUser("user");
        ds.setPassword("password");
        connection = ds.getConnection();

//        Server server = Server.createTcpServer("-tcpPort", "9091", "-tcpAllowOthers").start();
    }

    public Room create(Room room) throws SQLException {
        InsertSql insertSql = new InsertSql(connection, room);
        PreparedStatement createPs = insertSql.getPreparedStatement();

        if(createPs.executeUpdate() > 0){
            ResultSet lastInsertId = connection.prepareStatement("SELECT LAST_INSERT_ID()").executeQuery();
            lastInsertId.next();

            PreparedStatement ps = connection.prepareStatement(SELECT_BY_ROOMID);
            ps.setInt(1, lastInsertId.getInt("LAST_INSERT_ID()"));

            ResultSet result = ps.executeQuery();
            result.next();

            Room createdRoom = new Room(result);
            createdRoom.setRoomid(result.getInt("roomid"));

            return createdRoom;
        } else {
            return null;
        }
    }

    public Room query(int id) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(SELECT_BY_ROOMID);
        ps.setInt(1, id);

        ResultSet result = ps.executeQuery();
        result.next();

        return new Room(result);
    }

    public Boolean delete(int bookingid) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(DELETE_BY_ROOMID);
        ps.setInt(1, bookingid);

        int resultSet = ps.executeUpdate();
        return resultSet == 1;
    }

    public Room update(int id, Room room) throws SQLException {
        UpdateSql updateSql = new UpdateSql(connection, id, room);
        PreparedStatement updatePs = updateSql.getPreparedStatement();

        if(updatePs.executeUpdate() > 0){
            PreparedStatement ps = connection.prepareStatement(SELECT_BY_ROOMID);
            ps.setInt(1, id);

            ResultSet result = ps.executeQuery();
            result.next();

            Room createdRoom = new Room(result);
            createdRoom.setRoomid(result.getInt("roomid"));

            return createdRoom;
        } else {
            return null;
        }
    }

    public List<Room> queryRooms() throws SQLException {
        List<Room> listToReturn = new ArrayList<Room>();

        PreparedStatement ps = connection.prepareStatement(SELECT_ROOMS);

        ResultSet results = ps.executeQuery();
        while(results.next()){
            listToReturn.add(new Room(results));
        }

        return listToReturn;
    }

    public void resetDB() throws SQLException {
        PreparedStatement ps = connection.prepareStatement(DELETE_ALL_ROOMS);

        ps.executeUpdate();

        PreparedStatement resetPs = connection.prepareStatement("ALTER TABLE PUBLIC.ROOMS ALTER COLUMN roomid RESTART WITH 1");

        resetPs.execute();
    }
}
