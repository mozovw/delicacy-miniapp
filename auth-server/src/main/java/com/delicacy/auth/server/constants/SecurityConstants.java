package com.delicacy.auth.server.constants;

public interface SecurityConstants {
    String PREFIX = "oauth:";
    String CLIENT_DETAILS_KEY = PREFIX + "client:details";
    String USER_DETAILS_KEY = PREFIX + "user:details";

    //默认退出登陆后删除的cookies
    String[] DEFAULT_SIGNOUT_DELETE_COOKIES = new String[]{"JSESSIONID", "SESSIONID"};

    // oauth_client_details 表的字段，不包括client_id、client_secret
    String CLIENT_FIELDS_FOR_UPDATE = "resource_ids, scope, "
            + "authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, "
            + "refresh_token_validity, additional_information, autoapprove";
    String CLIENT_FIELDS = "client_secret, " + CLIENT_FIELDS_FOR_UPDATE;

    //  查询语句
    String BASE_FIND_STATEMENT = "select client_id," + CLIENT_FIELDS + " from zg_oauth_client_details";

    // 默认的查询语句
    String DEFAULT_FIND_STATEMENT = BASE_FIND_STATEMENT + " order by client_id";

    // 按条件client_id 查询

    String DEFAULT_SELECT_STATEMENT = BASE_FIND_STATEMENT + " where client_id = ?";

    //  插入
    String DEFAULT_INSERT_STATEMENT = "insert into oauth_client_details (" + CLIENT_FIELDS
            + ", client_id) values (?,?,?,?,?,?,?,?,?,?,?)";

}
