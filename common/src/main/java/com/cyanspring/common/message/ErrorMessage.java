package com.cyanspring.common.message;

public enum ErrorMessage {
	// exception start with 000
	EXCEPTION_MESSAGE,
	NONE_MATCHED_MESSAGE,
	EMPTY_MESSAGE,
	// system errors start with 100
	RECEIVE_HANDLE_ERROR,
	ACTIVEMQ_CONNECT_FAILED,
	ACTIVEMQ_CLOSE_FAILED,
	ACTIVEMQ_TIMEOUT,
	ACTIVE_IDLE,
	PACKET_FORMAT_WRONG,
	CONNECTION_NOT_AVAILABLE,
	CONNECTION_BROKEN,
	INVALID_CMD_PACKET,
	UNSUPPORTED_CMD_PACKET,
	SERVER_NOT_AVAILABLE,
	SEND_FRAME_FAILED,
	NONE_SET_USER,
	NONE_SET_CALLBACK,
	NONE_SET_CONNECTION_CONFIG,
	SYSTEM_NOT_READY,
	SERVER_NOT_READY_FOR_LOGIN,
	SERVER_SUSPEND,
	FAST_REJECT,

	// business errors start with 200
	PREMIUM_FOLLOW_INFO_INCOMPLETE,
	PREMIUM_FOLLOW_REQUEST_TIMEOUT,
	SYMBOLIST_ERROR,
	SEARCH_SYMBOL_ERROR,
	PRICE_ALERT_ERROR,
	TRADE_ALERT_ERROR,
	NO_ALERT_DATA,
	REF_SYMBOL_NOT_FOUND, // Ref packet's symbols not found
	REF_SYMBOL_DUPLICATED,
	REF_SYMBOL_NOT_VALID, // Ref packet's symbols not valid
	REF_SYMBOL_RUNTIME_ERROR, // Ref packet's symbols runtime error
	USER_HAVE_NOT_SUBSCRIBE_SYMBOL,// User didn't subscribe symbol
	USER_NONE_PERMISSION, // User doesn't have enough permission
	SUBSCRIBE_LIMITATION, // Subscribe count is limited
	SEQUENCE_NUMBER_ERROR,
	NOT_SUPPORT_FREQUENCE,//Chart Error
	SYMBOL_NOT_FOUND,
	POSITION_NOT_FOUND,
	POSITION_PENDING,
	STRATEGY_NOT_REGISTERD,
	STRATEGY_ERROR,
	STRATEGY_NOT_PRESENT_IN_SINGLE_INSTRUMENT,
	SNIPER_STRATEGY_PRICE_EMPTY,
	STRATEGY_NOT_DEFINED_IN_REGISTRY,
	FIX_EXCEPTION,
	DOWN_STREAM_EXCEPTION,
	DOWN_STREAM_NULL_LISTENER,
	DOWN_STREAM_NULL_FIELDS,
	ORDER_NOT_IN_READY_STATUS,
	FIX_FIELD_NOT_DEFINED_STRATEGY,
	FIX_STRATEGY_FIELD_IS_NOT_PRESENTED,
	FIX_SIDE_NOT_HANDLED,
	FIX_SIDE_CANT_CONVERT,
	FIX_CANT_MAP_SIDE,
	FX_CONVERTER_CANT_FIND_SYMBOL,
	FX_CONVERTER_RATE_IS_ZERO,
	DOWN_STREAM_TOO_MANY_LISTENER,
	POSITION_CONTAINS_DIFF_ACCOUNT,
	POSITION_CONTAINS_DIFF_SYMBOL,
	POSITION_CONTAINS_DIFF_SIDE,
	POSITION_CONTAINS_ZERO,
	UPSTREAM_EXCEPTION,
	UPSTREAM_CONNECTION_EXIST,
	DATA_CONVERT_EXCEPTION,
	OVER_SET_MAX_PRICEALERTS,
	DATA_CONVERT_CLASS_NULL,
	DATA_CONVERT_UNKNOWN_DATE_FORMAT,
	DATA_CONVERT_CONVERT_FIELD_FAIL,
	DOWN_STREAM_CONN_ID_EXIST,
	DOWN_STREAM_CONN_DOWN,
	DOWN_STREAM_SENDER_NOT_AVAILABLE,
	TICK_DATA_LESS_THAN_ONE_TOKEN,
	TICK_DATA_TAG_VALUE_MALFORMATTED,
	TICK_DATA_FIRST_FIELD_MUST_SYMBOL,
	TICK_DATA_EXCEPTION,
	TICK_DATA_ASK_VOL_OUT_OF_SEQ,
	TICK_DATA_BID_VOL_OUT_OF_SEQ,
	COIN_TYPE_NOT_FOUND,
	COIN_END_DATE_NOT_SETTING,

	// api errors start with 300
	SEVER_NOT_CONNECTED,
	USER_NEED_LOGIN_BEFORE_EVENTS,
	EVENT_TYPE_NOT_SUPPORT,
	ACCOUNT_NOT_MATCH,
	LOGIN_BLOCKED,
	REACH_MAX_ACCESS_LIMIT,
	VERSION_NEED_UPDATE,
	ACCOUNT_PERM_DENIED,

	// order errors start with 400
	ACTION_CANCELLED,
	ORDER_PROCESSING,
	ORDER_NOT_CAHNGE,
	ORDER_ID_NOT_FOUND,
	ORDER_SIDE_NOT_SUPPORT,
	ORDER_TYPE_NOT_SUPPORT,
	ACCOUNT_NOT_EXIST,
	USER_NOT_LOGIN,
	ENTER_ORDER_ERROR,
	AMEND_ORDER_ERROR,
	CANCEL_ORDER_ERROR,
	AMEND_PARAM_SHORT,
	CLOSE_ORDER_ERROR,
	TX_ID_ILLEGAL,
	ORDER_REASON_NOT_SUPPORT,
	ALERT_TYPE_NOT_SUPPORT,
	NONE_ASSIGN_PRICE,
	AMEND_ORDER_NOT_FOUND,
	ORDER_ALREADY_COMPLETED,
	ORDER_ALREADY_TERMINATED,
	ORDER_VALIDATION_ERROR,
	FIELD_DEFINITION_NOT_FOUND,
	CANCEL_ORDER_NOT_FOUND,
	ORDER_ID_EXIST,
	ORDER_IS_PENDING,
	OVER_FILLED,
	NO_ORDER_IN_ACTIVE_CHILD_ORDER,
	CUM_QTY_GREATER_THAN_INTENTED_QTY,
	PRICE_NOT_PERMITTED,
	PARENT_ORDER_IS_PENDING,
	MARKET_CLOSED,
	MARKET_VALIDATION_ERROR,
	ORDER_QTY_OVER_MAX_HOLD,
	ORDER_QTY_OVER_MAX_LOT,
	VALIDATION_ERROR,
	ORDER_FIELD_EMPTY,
	ORDER_FIELD_MUST_GREATER_THAN_ZERO,
	ORDER_FIELD_MUST_BE_INTEGER,
	ORDER_SYMBOL_LOT_SIZE_ERROR,
	ORDER_SYMBOL_NOT_FOUND,
	INVALID_QUANTITY,
	ORDER_ACCOUNT_OVER_CREDIT_LIMIT,
	AMEND_ORDER_OVER_CREDIT_LIMIT,
	ENDTIME_IN_THE_PASS,
	STARTTIME_SAME_AS_ENDTIME,
	ICEBERG_STRATEGY_QTY_EMPTY,
	DAILY_ORDERS_EXCEED_LIMIT,
	DAILY_ORDERS_EXCEED_LIMIT_CAN_AMEND_QTY,
	STRATEGY_IS_NOT_DEFINED,
	STRATEGY_PARAMS_IS_MISSING,
	ORDER_FIELD_VALUE_IS_EMPTY,
	ORDER_FIELD_OUT_OF_RANGE,
	STOP_LOSS_PRICE_EMPTY,
	STOP_LOSS_PRICE_CANT_OVER_THAN_LIMIT_PRICE,
	ORDER_CANT_CONVERT_TO_FIX_TYPE,
	ORDER_OVER_CEIL_PRICE,
	ORDER_LOWER_FLOOR_PRICE,
	ORDER_CANT_FIND_QUOTEEXT_FILE,
	LIVE_TRADING_STOP_TRADING,
	MARKET_WILL_TAKE_ORDER_AFTER_OPEN,
	MARKET_WILL_TAKE_ORDER_BEFORE_OPEN_ONE_HOUR,
	SYMBOL_NOT_TRADABLE,
	TRADING_SUSPENSION,
	ORDER_QTY_OVER_MAX_SETTING,
	ORDER_QTY_NOT_MET_MINIMUM_SETTING,
	ORDER_OVER_ACCOUNT_VALUE_PERCENTAGE,
	
	// user errors start with 500
	CREATE_USER_FAILED,
	USER_LOGIN_FAILED,
	CHANGE_USER_PWD_FAILED,
	USER_LOGIN_APPSERVER_FAILED,
	WRONG_USER_TYPE,
	EMPTY_PWD,
	PREMIUM_FOLLOW_ERROR,
	ACCOUNT_RESET_ERROR,
	INVALID_USER_INFO,
	NO_TRADING_ACCOUNT,
	INVALID_USER_ACCOUNT_PWD,
	USER_ALREADY_EXIST,
	CREATE_DEFAULT_ACCOUNT_ERROR,
	USER_EMAIL_EXIST,
	ACCOUNT_AND_USER_NOT_MATCH,
	QUANTITY_EXCEED_AVAILABLE_QUANTITY,
	USER_IS_TERMINATED,
	TERMINATE_USER_FAILED,
	ACCOUNT_FROZEN,
	ACCOUNT_TERMINATED,
	THIRD_PARTY_ID_NOT_MATCH_USER_ID,
	THIRD_PARTY_ID_REGISTER_FAILED,
	DETACH_THIRD_PARTY_ID_FAILED,
    USER_PHONE_EXIST,
	THIRD_PARTY_ID_USED_IN_NEW_APP,
    LIVE_TRADING_SETTING_NOT_OVER_FROZEN_DAYS,
    LIVE_TRADING_NO_RULE_IN_MAP,
	ATTACH_THIRD_PARTY_ID_FAILED,
	USER_POSITION_STOP_LOSS_VALUE_EXCEEDS_COMPANY_SETTING,
	USER_DAILY_STOP_LOSS_VALUE_EXCEEDS_COMPANY_SETTING,
	FDT_ID_IS_UNDER_PROCESSING,
	ACCOUNT_ALREADY_ACTIVE,
	OVER_TERMINATE_LOSS,
	OVER_FROZEN_LOSS,
	CREATE_GROUP_MANAGEMENT_FAILED,
	GET_GROUP_MANAGEMENT_INFO_FAILED,
	CSTW_LOGIN_FAILED,
	DELETE_GROUP_MANAGEMENT_FAILED,
	CHANGE_USER_ROLE_FAILED,
	FREEZE_ACCOUNT_FAILED,
	CHANGE_ACCOUNT_STATE_FAILED,

	// client errors start with 600
	NEED_RESTART_APP,
	SERVER_IN_MAINTAINING,
	ANOTHER_DEVICE_ALREADY_LOGIN,
	NEW_VERSION_NEED_DOWNLOAD,
	NEW_VERSION_AVAILABLE,
	MESSAGE_ARRIVED,
	CLIENT_NOT_CONNECT,
	CLIENT_NOT_DISCONNECT,
	CLIENT_PARAM_ERROR,
	CLIENT_IDLE,
	CLIENT_SEND_DISCONNECT,
	CLIENT_VERSION_ERROR,

	//DB errors start with 700
	SQL_SYNTAX_ERROR,
	DATA_NOT_FOUND,
	DATA_ALREADY_EXIST,
	WRONG_ACTION,
	CANT_CONNECT_TO_CENTRAL_DATABASE,

	//quote errors start with 800
	INVALID_QUOTE_ID,
	QUOTE_NOT_SUBSCRIBE,
	NO_QUOTE_DATA,
	NO_DATA_ERROR,
	NO_MORE_DATA,

	// Admin -> ExchangeAccount/ExchangeSubAccount/InstrumentPool/InstrumentPoolRecord errors start with 900
	EXCHANGE_ACCOUNT_ALREADY_EXISTS,
	EXCHANGE_ACCOUNT_NOT_FOUND,
	EXCHANGE_ACCOUNT_HAS_DEPENDENCY,
	EXCHANGE_SUB_ACCOUNT_ALREADY_EXISTS,
	EXCHANGE_SUB_ACCOUNT_NOT_FOUND,
	EXCHANGE_SUB_ACCOUNT_HAS_DEPENDENCY,
	INSTRUMENT_POOL_ALREADY_EXISTS,
	INSTRUMENT_POOL_NOT_FOUND,
	INSTRUMENT_POOL_RECORD_NOT_FOUND,
	ACCOUNT_POOL_IS_NULL,
	USER_EXCHANGE_SUB_ACCOUNT_IS_NULL,
	USER_EXCHANGE_SUB_ACCOUNT_ALREADY_EXISTS,
}
