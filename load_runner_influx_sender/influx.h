#ifndef _INFLUX_H
#define _INFLUX_H

#include <stdio.h>

/*
 This file unite functions for influx integration.
 ------------------------------------
 Tips:
 - Add influx_defineIntegration() function in vuser_init() of your script. It has to be called once for proper work.
 - After that lr_start_transaction() and lr_end_transaction() functions will be overrided. Call them to save execution time in influx DB.
 - These functions works only in web/http protocol.
 - Change INFLUX_HOST, DB_NAME and MEASUREMENT_NAME constants before starting.
*/

#define SIZE_BUFF 200												// depends on max transaction's name length
#define ARRAY_SIZE 100												// can depend on max threads number
#define TRANSACTION_FAIL "fail"
#define TRANSACTION_PASS "pass"


// changable, depends on influx parameters
// -----------------------------------------------------------------------------------------------------------
#define INFLUX_HOST "http://172.30.48.58:8510"
#define INFLUX_DB_NAME "resultsdb"
#define INFLUX_MEASUREMENT_NAME "jmeter"
#define NODE_NAME_DEFAULT "default"
#define APPLICATION_NAME "loadRunner"
// -----------------------------------------------------------------------------------------------------------


// methods prototypes
// -----------------------------------------------------------------------------------------------------------
void influx_defineIntegration();

int influx_customStartTransaction(char* pTransactionName);
int influx_customEndTransaction(char* pTransactionName, int nTransStatus);

void influx_initializeHandlesArray();
void influx_setNewTransaction(char* pTrName, long lTrHandle);
long influx_getTransactionHandle(char* pTrName);
int influx_getFreeArrayIndex();

void influx_sendParam(char* pTransactionName, int nTrStatus, double dTrDuration);
// -----------------------------------------------------------------------------------------------------------


/*
	struct for transactions names and handles
*/
struct handlesStruct{
	long handle;
	char name[SIZE_BUFF];
};

/*
	array of structs with transactions info
*/
struct handlesStruct arrayOfStructs[ARRAY_SIZE];

/*
	enabling influx integration.
	everrides default methods
*/
void influx_defineIntegration() {
	#define _INFLUX_INTEGRATE
	influx_initializeHandlesArray();
}

/*
	overriding default methods
*/
#ifdef _INFLUX_INTEGRATE
	#define lr_start_transaction(x) influx_customStartTransaction(x)
    #define lr_end_transaction(x, y) influx_customEndTransaction(x, y)
#endif // _INFLUX_INTEGRATE

/*
	custom transaction starting
*/
int influx_customStartTransaction(char* pTransactionName) {
	if (pTransactionName == NULL) {
		lr_error_message("transaction name is null");
		return -1;
	}
	//
	if (strlen(pTransactionName) <= 0) {
		lr_error_message("transaction name is empty");
		return -1;
	}
	//
	influx_setNewTransaction(pTransactionName, lr_start_transaction_instance(pTransactionName, 0));
	return 0;
}

/*
	custom transaction ending
*/
int influx_customEndTransaction(char* pTransactionName, int nTransStatus) {
	long lHandle = 0;
	double dDuration = 0;
	int nStatus = nTransStatus;
	//
	if (pTransactionName == NULL) {
		lr_error_message("transaction name is null");
		return -1;
	}
	//
	if (strlen(pTransactionName) <= 0) {
		lr_error_message("transaction name is empty");
		return -1;
	}
	//
	lHandle = influx_getTransactionHandle(pTransactionName);
	dDuration = lr_get_trans_instance_duration(lHandle);
	//
	if (lHandle < 0) {
		lr_error_message("handle is null in transaction %s", pTransactionName);
		return -1;
	}
	//
	if (nStatus == LR_AUTO)
		nStatus = lr_get_trans_instance_status(lHandle);
	//
	influx_sendParam(pTransactionName, nStatus, dDuration);
	return lr_end_transaction_instance(lHandle, nStatus);
}

/*
	initializing array of structs with handles and transaction names
*/
void influx_initializeHandlesArray() {
	memset(arrayOfStructs, 0 , sizeof(arrayOfStructs));
}

/*
	adding transaction with its handle into array
*/
void influx_setNewTransaction(char* pTrName, long lTrHandle) {
	int nFreeIndex = 0;
	int nTransLength = 0;
	//
	if (pTrName == NULL)
		return;
	//
	nFreeIndex = influx_getFreeArrayIndex();
	nTransLength = strlen(pTrName);
	//
	if (nFreeIndex < 0) {
		lr_error_message("no free index found while evaluating transaction %s , with handle %d", pTrName, lTrHandle);
		return;
	}
	//
	if (nTransLength >= SIZE_BUFF) {
		lr_error_message("too long transaction name! The name: \"%s\" is grater then buffer size: %d", pTrName, SIZE_BUFF);
		return;
	}
	//
	strncpy(arrayOfStructs[nFreeIndex].name, pTrName, nTransLength);
	arrayOfStructs[nFreeIndex].handle = lTrHandle;
}

/*
	get transaction handle from array and delete it
*/
long influx_getTransactionHandle(char* pTrName) {
	int i = 0;
	long lResultHandle = 0;
	//
	if (pTrName == NULL)
		return -1;
	//
	for (i = 0; i < ARRAY_SIZE; ++i) {
		lr_output_message("array cycle in transaction: %s, iterator: %d, name value: %s, handle value: %d", pTrName, i, arrayOfStructs[i].name, arrayOfStructs[i].handle);
		//		
		if (strlen(arrayOfStructs[i].name) <= 1)
			continue;
		//
		if (strstr(arrayOfStructs[i].name, pTrName) && (strlen(arrayOfStructs[i].name) == strlen(pTrName))) {
			lResultHandle = arrayOfStructs[i].handle;
			memset(&arrayOfStructs[i], 0 , sizeof(arrayOfStructs[i]));
			break;
		}
	}
	//
	return lResultHandle;
}

/*
	get next free index from array, using while adding new transaction info
*/
int influx_getFreeArrayIndex() {
	int i = 0;
	//
	for (i = 0; i < ARRAY_SIZE; ++i) {
		if ((strlen(arrayOfStructs[i].name) == 0) && (arrayOfStructs[i].handle == 0))
			return i;
	}
	//
	return -1;
}

/*
	sending transaction info into influx
*/
void influx_sendParam(char* pTransactionName, int nTrStatus, double dTrDuration) {
	int id, scid;
	char* vuser_group;												// thread info
	char* current_node;												// current node name
	//
	char* pStatus;
	int nMILLISDuration = dTrDuration * 1000;
	pStatus = TRANSACTION_PASS;
	//
	if (pTransactionName == NULL)
		return;
	//
	if (nTrStatus == LR_FAIL)
		pStatus = TRANSACTION_FAIL;
	//
	if (nMILLISDuration < 0)
		nMILLISDuration = 1000;										// we get negative value if transaction fails, should be positive as default (1sec)
	//
	// debug
//	lr_output_message("transactionName: %s", pTransactionName);
//	lr_output_message("transactionDuration: %f", dTrDuration);
//	lr_output_message("transactionDuration: %d", nMILLISDuration);
//	lr_output_message("transactionStatus: %s", pStatus);
	//
	lr_whoami(&id, &vuser_group, &scid);							// get thread info	// Thread Group 1-4 - jmeter // Vuser id: 1, group: HTTP_UC01_filter, scenario id: 0 - loadrunner
	current_node = lr_get_host_name();								// get current host info
	//
	lr_param_sprintf("influxUrl_param", "%s/write?db=%s", INFLUX_HOST, INFLUX_DB_NAME);
	//
	lr_save_string(INFLUX_MEASUREMENT_NAME, "influxMeasurementName_param");
	lr_save_string(current_node == NULL ? NODE_NAME_DEFAULT : current_node, "currentNode_param");
	lr_save_string(APPLICATION_NAME, "applicationName_param");
	lr_save_string(pTransactionName, "pTransactionName_param");
	lr_save_string(pStatus, "pStatus_param");
	lr_param_sprintf("threadName_param", "%s_%d", vuser_group, id);
	lr_save_int(nMILLISDuration, "nMILLISDuration_param");	
	//
	web_custom_request("send influx info",
       "Method=POST",
       "URL={influxUrl_param}",
       "Body={influxMeasurementName_param},node={currentNode_param},application={applicationName_param},transaction={pTransactionName_param},status={pStatus_param},thread={threadName_param} value={nMILLISDuration_param}",
       LAST);
	//
	lr_free_parameter("influxUrl_param");
	lr_free_parameter("influxMeasurementName_param");
	lr_free_parameter("currentNode_param");
	lr_free_parameter("applicationName_param");
	lr_free_parameter("pTransactionName_param");
	lr_free_parameter("pStatus_param");
	lr_free_parameter("threadName_param");
	lr_free_parameter("nMILLISDuration_param");
}


#endif // _INFLUX_H