package com.nest.ib.contract;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 3.3.1.
 */
public class OfferContract extends Contract {
    private static final String BINARY = "60806040526000600860006101000a81548160ff02191690831515021790555034801561002b57600080fd5b5060405161121a38038061121a833981810160405260a081101561004e57600080fd5b81019080805190602001909291908051906020019092919080519060200190929190805190602001909291908051906020019092919050505080600860016101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550600860019054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16638fe77e866040518163ffffffff1660e01b815260040180806020018281038252600c8152602001807f6f66666572466163746f7279000000000000000000000000000000000000000081525060200191505060206040518083038186803b15801561016c57600080fd5b505afa158015610180573d6000803e3d6000fd5b505050506040513d602081101561019657600080fd5b8101908080519060200190929190505050600960006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550600960009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161461024157600080fd5b326000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550846001819055508360028190555082600360006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550846004819055508360058190555081600781905550436006819055505050505050610f1a806103006000396000f3fe6080604052600436106100c25760003560e01c80637aae3c6d1161007f578063b4f58a7611610059578063b4f58a7614610329578063b6bc035414610354578063cdbce03d14610383578063e4736a66146103da576100c2565b80637aae3c6d146102345780638fc3047d1461025f578063ab5d3366146102c4576100c2565b806304f0c14e146100c757806308ad57b51461011e57806313e24862146101285780631b1bddc91461015357806328a8ef1f146101b857806371aa8fd9146101cf575b600080fd5b3480156100d357600080fd5b506100dc610428565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b610126610452565b005b34801561013457600080fd5b5061013d610595565b6040518082815260200191505060405180910390f35b34801561015f57600080fd5b50610168610666565b604051808481526020018381526020018273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001935050505060405180910390f35b3480156101c457600080fd5b506101cd61069f565b005b3480156101db57600080fd5b506101e46108c6565b604051808481526020018381526020018273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001935050505060405180910390f35b34801561024057600080fd5b506102496109eb565b6040518082815260200191505060405180910390f35b34801561026b57600080fd5b506102746109f5565b604051808481526020018381526020018273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001935050505060405180910390f35b3480156102d057600080fd5b50610327600480360360608110156102e757600080fd5b810190808035906020019092919080359060200190929190803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610a2e565b005b34801561033557600080fd5b5061033e610b8d565b6040518082815260200191505060405180910390f35b34801561036057600080fd5b50610369610b97565b604051808215151515815260200191505060405180910390f35b34801561038f57600080fd5b50610398610bae565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b610426600480360360408110156103f057600080fd5b8101908080359060200190929190803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610bd7565b005b6000600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905090565b600960009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16146104ac57600080fd5b600254600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166370a08231306040518263ffffffff1660e01b8152600401808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060206040518083038186803b15801561054e57600080fd5b505afa158015610562573d6000803e3d6000fd5b505050506040513d602081101561057857600080fd5b81019080805190602001909291905050501461059357600080fd5b565b6000600960009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663cbc7ef096040518163ffffffff1660e01b815260040160206040518083038186803b1580156105ff57600080fd5b505afa158015610613573d6000803e3d6000fd5b505050506040513d602081101561062957600080fd5b810190808051906020019092919050505061064f60065443610dec90919063ffffffff16565b111561065e5760019050610663565b600090505b90565b6000806000600454600554600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16925092509250909192565b600960009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16146106f957600080fd5b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163273ffffffffffffffffffffffffffffffffffffffff161461075257600080fd5b600161075c610595565b1461076657600080fd5b60001515600860009054906101000a900460ff1615151461078657600080fd5b6000806107916108c6565b5080925081935050506107c56000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1683610e0c565b600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663a9059cbb6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff16836040518363ffffffff1660e01b8152600401808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200182815260200192505050600060405180830381600087803b15801561088f57600080fd5b505af11580156108a3573d6000803e3d6000fd5b505050506001600860006101000a81548160ff0219169083151502179055505050565b60008060003073ffffffffffffffffffffffffffffffffffffffff1631600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166370a08231306040518263ffffffff1660e01b8152600401808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060206040518083038186803b15801561098257600080fd5b505afa158015610996573d6000803e3d6000fd5b505050506040513d60208110156109ac57600080fd5b8101908080519060200190929190505050600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16925092509250909192565b6000600654905090565b6000806000600154600254600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16925092509250909192565b600960009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614610a8857600080fd5b6000610a92610595565b14610a9c57600080fd5b826004541015610aab57600080fd5b816005541015610aba57600080fd5b600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff1614610b1457600080fd5b610b3d600454610b2f85600554610e7b90919063ffffffff16565b610eb590919063ffffffff16565b8214610b4857600080fd5b610b523284610e0c565b610b6783600454610dec90919063ffffffff16565b600481905550610b8282600554610dec90919063ffffffff16565b600581905550505050565b6000600754905090565b6000600860009054906101000a900460ff16905090565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905090565b600960009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614610c3157600080fd5b6000610c3b610595565b14610c4557600080fd5b346004541015610c5457600080fd5b816005541015610c6357600080fd5b600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff1614610cbd57600080fd5b610ce6600454610cd834600554610e7b90919063ffffffff16565b610eb590919063ffffffff16565b8214610cf157600080fd5b600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663a9059cbb32846040518363ffffffff1660e01b8152600401808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200182815260200192505050600060405180830381600087803b158015610d9a57600080fd5b505af1158015610dae573d6000803e3d6000fd5b50505050610dc734600454610dec90919063ffffffff16565b600481905550610de282600554610dec90919063ffffffff16565b6005819055505050565b600082821115610dfb57600080fd5b600082840390508091505092915050565b6000610e2d8373ffffffffffffffffffffffffffffffffffffffff16610edb565b90508073ffffffffffffffffffffffffffffffffffffffff166108fc839081150290604051600060405180830381858888f19350505050158015610e75573d6000803e3d6000fd5b50505050565b600080831415610e8e5760009050610eaf565b6000828402905082848281610e9f57fe5b0414610eaa57600080fd5b809150505b92915050565b6000808211610ec357600080fd5b6000828481610ece57fe5b0490508091505092915050565b600081905091905056fea265627a7a723158209bb3265a3a9201b431f2ee43112fe541ff08d67408cc6f7c84117a14ad6739b564736f6c634300050c0032";

    protected OfferContract(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected OfferContract(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static RemoteCall<OfferContract> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, BigInteger _ethAmount, BigInteger _tokenAmount, String _tokenAddress, BigInteger miningEth, String map) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Uint256(_ethAmount),
                new Uint256(_tokenAmount),
                new Address(_tokenAddress),
                new Uint256(miningEth),
                new Address(map)));
        return deployRemoteCall(OfferContract.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static RemoteCall<OfferContract> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, BigInteger _ethAmount, BigInteger _tokenAmount, String _tokenAddress, BigInteger miningEth, String map) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Uint256(_ethAmount),
                new Uint256(_tokenAmount),
                new Address(_tokenAddress),
                new Uint256(miningEth),
                new Address(map)));
        return deployRemoteCall(OfferContract.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public RemoteCall<TransactionReceipt> changeOfferErc(BigInteger _ethAmount, BigInteger _tokenAmount, String _tokenAddress) {
        final Function function = new Function(
                "changeOfferErc", 
                Arrays.<Type>asList(new Uint256(_ethAmount),
                new Uint256(_tokenAmount),
                new Address(_tokenAddress)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> changeOfferEth(BigInteger _tokenAmount, String _tokenAddress, BigInteger weiValue) {
        final Function function = new Function(
                "changeOfferEth", 
                Arrays.<Type>asList(new Uint256(_tokenAmount),
                new Address(_tokenAddress)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteCall<Tuple3<BigInteger, BigInteger, String>> checkAssets() {
        final Function function = new Function("checkAssets", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}));
        return new RemoteCall<Tuple3<BigInteger, BigInteger, String>>(
                new Callable<Tuple3<BigInteger, BigInteger, String>>() {
                    @Override
                    public Tuple3<BigInteger, BigInteger, String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<BigInteger, BigInteger, String>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (String) results.get(2).getValue());
                    }
                });
    }

    public RemoteCall<BigInteger> checkBlockNum() {
        final Function function = new Function("checkBlockNum", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> checkContractState() {
        final Function function = new Function("checkContractState", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<Tuple3<BigInteger, BigInteger, String>> checkDealAmount() {
        final Function function = new Function("checkDealAmount", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}));
        return new RemoteCall<Tuple3<BigInteger, BigInteger, String>>(
                new Callable<Tuple3<BigInteger, BigInteger, String>>() {
                    @Override
                    public Tuple3<BigInteger, BigInteger, String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<BigInteger, BigInteger, String>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (String) results.get(2).getValue());
                    }
                });
    }

    public RemoteCall<Boolean> checkHadReceive() {
        final Function function = new Function("checkHadReceive", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<String> checkOwner() {
        final Function function = new Function("checkOwner", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<Tuple3<BigInteger, BigInteger, String>> checkPrice() {
        final Function function = new Function("checkPrice", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}));
        return new RemoteCall<Tuple3<BigInteger, BigInteger, String>>(
                new Callable<Tuple3<BigInteger, BigInteger, String>>() {
                    @Override
                    public Tuple3<BigInteger, BigInteger, String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<BigInteger, BigInteger, String>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (String) results.get(2).getValue());
                    }
                });
    }

    public RemoteCall<BigInteger> checkServiceCharge() {
        final Function function = new Function("checkServiceCharge", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<String> checkTokenAddress() {
        final Function function = new Function("checkTokenAddress", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> offerAssets(BigInteger weiValue) {
        final Function function = new Function(
                "offerAssets", 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteCall<TransactionReceipt> turnOut() {
        final Function function = new Function(
                "turnOut", 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public static OfferContract load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new OfferContract(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static OfferContract load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new OfferContract(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }
}
