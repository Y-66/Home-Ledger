package com.yu.ledger.service;

import java.util.List;
import java.util.Map;

public interface ILoanContractsService {
    List<Map<String, Object>> listContracts(Map<String, Object> params);
    Map<String, Object> addContract(Map<String, Object> contract);
    Map<String, Object> updateContract(String contractId, Map<String, Object> contract);
    Map<String, Object> closeContract(String contractId);
    Map<String, Object> getContractDetail(String contractId);
    List<Map<String, Object>> getContractSchedules(String contractId);
    List<Map<String, Object>> contractOptions(Integer customerId);
}
