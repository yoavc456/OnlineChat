package utils

/*
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

private val url:String = "jdbc:mysql://localhost:3306/chatdb"
private val sql_username:String = "root"
private val sql_password:String = "1234"

fun isUserExist(username:String, password:String):Boolean{
    var connection: Connection? = null
    var statement: Statement? = null
    var resultSet: ResultSet? = null

    try {
        connection = DriverManager.getConnection(url, sql_username, sql_password)
        statement = connection.createStatement()

        resultSet = statement.executeQuery("SELECT * FROM users WHERE username = '$username'")

        if(resultSet.next() && resultSet.getString("password").equals(password))
            return true

    }catch (e:Exception){
        e.printStackTrace()
    }finally {
        close(connection, statement, resultSet)
    }

    return false
}

fun isUsernameExist(username: String):Boolean{
    var connection: Connection? = null
    var statement: Statement? = null
    var resultSet: ResultSet? = null

    try {
        connection = DriverManager.getConnection(url, sql_username, sql_password)
        statement = connection.createStatement()

        resultSet = statement.executeQuery("SELECT * FROM users WHERE username = '$username'")

        if(resultSet.next())
            return true

    }catch (e:Exception){
        e.printStackTrace()
    }finally {
        close(connection, statement, resultSet)
    }

    return false
}

fun createUser(username: String, password: String):Boolean{
    var connection: Connection? = null
    var statement: Statement? = null
    var resultSet: ResultSet? = null

    try {
        connection = DriverManager.getConnection(url, sql_username, sql_password)
        statement = connection.createStatement()

        statement.execute("INSERT INTO users VALUES(default, '$username', '$password');")
        return true
    }catch (e:Exception){
        e.printStackTrace()
    }finally {
        close(connection, statement, resultSet)
    }

    return false
}

private fun close(connection: Connection?, statement: Statement?, resultSet: ResultSet?){
    if (connection != null)
        connection.close()

    if (statement != null)
        statement.close()

    if (resultSet != null)
        resultSet.close()
}

 */