package com.ctrip.platform.dal.daogen.dao;

import com.ctrip.platform.dal.daogen.entity.GenTaskByTableViewSp;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DaoByTableViewSp {
    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * 根据项目主键查询所有任务
     *
     * @param iD
     * @return
     */
    public List<GenTaskByTableViewSp> getTasksByProjectId(int iD) {
        return jdbcTemplate.query(
                "SELECT id, project_id,db_name,table_names,view_names,sp_names,prefix,suffix,"
                        + "cud_by_sp,pagination,`generated`,version,update_user_no,update_time,"
                        + "comment,sql_style,api_list,approved,approveMsg FROM task_table " + "WHERE project_id=?",
                new Object[] {iD}, new RowMapper<GenTaskByTableViewSp>() {
                    public GenTaskByTableViewSp mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return GenTaskByTableViewSp.visitRow(rs);
                    }
                });
    }

    /**
     * 根据task主键查询任务
     *
     * @param taskId
     * @return
     */
    public GenTaskByTableViewSp getTasksByTaskId(int taskId) {
        List<GenTaskByTableViewSp> list = jdbcTemplate.query(
                "SELECT id, project_id,db_name,table_names,view_names,sp_names,prefix,suffix,"
                        + "cud_by_sp,pagination,`generated`,version,update_user_no,update_time,"
                        + "comment,sql_style,api_list,approved,approveMsg FROM task_table " + "WHERE id=?",
                new Object[] {taskId}, new RowMapper<GenTaskByTableViewSp>() {
                    public GenTaskByTableViewSp mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return GenTaskByTableViewSp.visitRow(rs);
                    }
                });
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    public List<GenTaskByTableViewSp> updateAndGetAllTasks(int projectId) {
        final List<GenTaskByTableViewSp> tasks = new ArrayList<>();
        jdbcTemplate.query(
                "SELECT id, project_id,db_name,table_names,view_names,sp_names,prefix,suffix,cud_by_sp,"
                        + "pagination,`generated`,version,update_user_no,update_time,comment,"
                        + "sql_style,api_list,approved,approveMsg FROM task_table WHERE project_id=?",
                new Object[] {projectId}, new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        GenTaskByTableViewSp task = GenTaskByTableViewSp.visitRow(rs);
                        task.setGenerated(true);
                        if (updateTask(task) > 0) {
                            tasks.add(task);
                        }
                    }
                });
        return tasks;
    }

    public List<GenTaskByTableViewSp> updateAndGetTasks(int projectId) {
        final List<GenTaskByTableViewSp> tasks = new ArrayList<>();
        jdbcTemplate.query(
                "SELECT id, project_id,db_name,table_names,view_names,sp_names,prefix,suffix,"
                        + "cud_by_sp,pagination,`generated`,version,update_user_no,update_time,"
                        + "comment,sql_style,api_list,approved,approveMsg FROM task_table "
                        + "WHERE project_id=? AND `generated`=FALSE",
                new Object[] {projectId}, new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        GenTaskByTableViewSp task = GenTaskByTableViewSp.visitRow(rs);
                        task.setGenerated(true);
                        if (updateTask(task) > 0) {
                            tasks.add(task);
                        }
                    }
                });
        return tasks;
    }

    public int insertTask(GenTaskByTableViewSp task) {
        return jdbcTemplate.update("INSERT INTO task_table ( project_id,  db_name,table_names,view_names,sp_names,"
                + "prefix,suffix,cud_by_sp,pagination,`generated`,version,update_user_no,update_time,"
                + "comment,sql_style,api_list,approved,approveMsg)" + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                task.getProject_id(), task.getDatabaseSetName(), task.getTable_names(), task.getView_names(),
                task.getSp_names(), task.getPrefix(), task.getSuffix(), task.isCud_by_sp(), task.isPagination(),
                task.isGenerated(), task.getVersion(), task.getUpdate_user_no(), task.getUpdate_time(),
                task.getComment(), task.getSql_style(), task.getApi_list(), task.getApproved(), task.getApproveMsg());
    }

    public int getVersionById(int id) {
        try {
            return jdbcTemplate.queryForObject("SELECT version FROM task_table WHERE id =?", new Object[] {id},
                    new RowMapper<Integer>() {
                        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return rs.getInt(1);
                        }
                    });
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    public int updateTask(GenTaskByTableViewSp task) {
        return jdbcTemplate.update("UPDATE task_table SET project_id=?,db_name=?,table_names=?,view_names=?,sp_names=?,"
                + "prefix=?,suffix=?,cud_by_sp=?,pagination=?,`generated`=?,version=version+1,"
                + "update_user_no=?,update_time=?,comment=?,sql_style=?,"
                + "api_list=?,approved=?,approveMsg=? WHERE id=? AND version=?",

                task.getProject_id(), task.getDatabaseSetName(), task.getTable_names(), task.getView_names(),
                task.getSp_names(), task.getPrefix(), task.getSuffix(), task.isCud_by_sp(), task.isPagination(),
                task.isGenerated(), task.getUpdate_user_no(), task.getUpdate_time(), task.getComment(),
                task.getSql_style(), task.getApi_list(), task.getApproved(), task.getApproveMsg(), task.getId(),
                task.getVersion());
    }

    public int updateTask(int taskId, int approved, String approveMsg) {
        return jdbcTemplate.update("UPDATE task_table SET approved=?, approveMsg=? WHERE id=?", approved, approveMsg,
                taskId);
    }

    public int deleteTask(GenTaskByTableViewSp task) {
        return jdbcTemplate.update("DELETE FROM task_table WHERE id=?", task.getId());
    }

    public int deleteByProjectId(int id) {
        return jdbcTemplate.update("DELETE FROM task_table WHERE project_id=?", id);
    }

    public int deleteByServerId(int id) {
        return jdbcTemplate.update("DELETE FROM task_table WHERE server_id=?", id);
    }

}
