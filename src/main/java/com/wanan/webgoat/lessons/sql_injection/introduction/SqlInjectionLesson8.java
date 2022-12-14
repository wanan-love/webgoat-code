package com.wanan.webgoat.lessons.sql_injection.introduction;

import com.wanan.webgoat.container.LessonDataSource;
import com.wanan.webgoat.container.assignments.AssignmentEndpoint;
import com.wanan.webgoat.container.assignments.AssignmentHints;
import com.wanan.webgoat.container.assignments.AttackResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@RestController
@AssignmentHints(value = {"SqlStringInjectionHint.8.1", "SqlStringInjectionHint.8.2", "SqlStringInjectionHint.8.3", "SqlStringInjectionHint.8.4", "SqlStringInjectionHint.8.5"})
public class SqlInjectionLesson8 extends AssignmentEndpoint {

    private final LessonDataSource dataSource;
    public SqlInjectionLesson8(LessonDataSource dataSource){
        this.dataSource = dataSource;
    }
    @PostMapping("/SqlInjection/attack8")
    @ResponseBody
    public AttackResult completed(@RequestParam String name,@RequestParam String auth_tan){
        return injectableQueryConfidentiality(name,auth_tan);
    }
    protected AttackResult injectableQueryConfidentiality(String name,String auth_tan){
        StringBuffer output = new StringBuffer();
        String query = "SELECT * FROM employees WHERE last_name = '" + name + "'AND auth_tan = '"+auth_tan + "'";
        try(Connection connection = dataSource.getConnection()){
            try {
                Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                log(connection,query);
                ResultSet results= statement.executeQuery(query);
                if (results.getStatement() != null){
                    if (results.first()){
                        output.append(generateTable(results));
                        results.last();

                        if (results.getRow() > 1){
//                        ??????????????????
                            return success(this).feedback("sql-injection.8.success").output(output.toString()).build();
                        }else {
//                            ??????????????????
                            return failed(this).feedback("sql-injection.8.one").output(output.toString()).build();
                        }
                    }else {
//                        ????????????
                        return failed(this).feedback("sql-injection.8.no.results").build();
                    }

                }else {
                    return failed(this).build();
                }
            }catch (SQLException e){
                return failed(this).output("<br><span class='feedback-negative'>" + e.getMessage()+"</span>").build();
            }
        }catch (Exception e){
            return failed(this).output("<br><span class='feedback-negative'>"+e.getMessage()+"</span>").build();
        }
    }



    public static String generateTable(ResultSet results)throws SQLException{
        ResultSetMetaData  resultSetMetaData = results.getMetaData();
//        ???????????????????????????
        int numColumns = resultSetMetaData.getColumnCount();
//        ?????????????????????
        results.beforeFirst();
//        ?????????????????????????????????
        StringBuilder table = new StringBuilder();
//
        table.append("<table>");
        if (results.next()){
//          ???????????????????????????
            table.append("<tr>");
            for (int i = 1; i < (numColumns + 1);i ++ ){
//                ??????????????????
                table.append("<tr>" + resultSetMetaData.getColumnName(i) + "</th>");
            }
            table.append("</tr>");
            results.beforeFirst();
//            ?????????????????????????????????
            while (results.next()){
                table.append("<tr>");
                for (int i = 1;i< (numColumns + 1); i++ ){
//                    ???????????????????????????????????????????????????
                    table.append("<td>" + results.getString(i) + "</td>");
                }
                table.append("</tr>");
            }
        }else {
            table.append("Query Successful; however no data was returned form this query.");

        }
        table.append("</table>");
        return (table.toString());
    }
    public static void log(Connection connection,String action){
        action =action.replace('\'','"');
        Calendar cal = Calendar.getInstance();
//        ????????????????????????
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
//        ?????????????????????
        String time = sdf.format(cal.getTime());
        String logQuery = "INSERT INTO access_log (time,action) VALUES ('"+time+"','"+action+"')";
        try{
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
            statement.executeUpdate(logQuery);
//                    CONCUR_UPDATABLE ?????????????????????????????????
        } catch (SQLException e) {
            System.err.print(e.getMessage());
//            ?????????????????? ????????????
        }

    }
}
