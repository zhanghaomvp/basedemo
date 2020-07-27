package main

import (
	"crypto/elliptic"
	"encoding/asn1"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"github.com/flyinox/crypto/sm/sm2"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	peer "github.com/hyperledger/fabric/protos/peer"
	"github.com/shopspring/decimal"
	"math/big"
	"strconv"
	"sync"
	"time"
)

const (
	CREDIT = "CREDIT"
	CASH   = "CASH"

	RECHARGE       = "RECHARGE"
	CONSUME        = "CONSUME"
	RECOVER_CREDIT = "RECOVER_CREDIT"
	LIMIT_CHANGE   = "LIMIT_CHANGE"

	CHECKED = "CHECKED"
)

type SimpleChainCode struct {
}

/*
 订单信息
*/
type Order struct {
	// 交易单号
	TradeNo string `json:"tradeNo,omitempty"`
	// 企业社会信用代码
	CompanySocialCreditCode string `json:"companySocialCreditCode,omitempty"`
	// 员工身份证号
	IdentityCard string `json:"identityCard,omitempty"`
	// 员工钱包号
	EmployeeWalletNo string `json:"employeeWalletNo,omitempty"`
	// 商家社会信用代码
	BusinessSocialCreditCode string `json:"businessSocialCreditCode,omitempty"`
	// 交易金额
	Amount string `json:"amount,omitempty"`
	// 交易类型
	DealType string `json:"dealType,omitempty"`
	// 支付类型
	PayType string `json:"payType,omitempty"`
	// 订单状态
	Status string `json:"status,omitempty"`
	// 创建时间
	Created string `json:"created,omitempty"`
	// 公钥
	Upk string `json:"upk,omitempty"`
	// 签名
	Sign string `json:"sign,omitempty"`
}

/*
 商家钱包
*/
type BusinessWallet struct {
	// 商家社会信用代码
	BusinessSocialCreditCode string `json:"businessSocialCreditCode,omitempty"`
	// 商家信用余额
	BusinessCreditBalance string `json:"businessCreditBalance,omitempty"`
	// 商家现金余额
	BusinessCashBalance string `json:"businessCashBalance,omitempty"`
	// 交易金额
	Amount string `json:"amount,omitempty"`
	// 支付类型
	PayType string `json:"payType,omitempty"`
	// 交易单号
	TradeNo string `json:"tradeNo,omitempty"`
	// 创建时间
	Created string `json:"created,omitempty"`
	// 公钥
	Upk string `json:"upk,omitempty"`
	// 签名
	Sign string `json:"sign,omitempty"`
}

/*
 个人钱包
*/
type PersonalWallet struct {
	// 个人钱包号
	PersonalWalletNo string `json:"personalWalletNo,omitempty"`
	// 个人信用额度
	PersonalCreditLimit string `json:"personalCreditLimit,omitempty"`
	// 个人信用余额
	PersonalCreditBalance string `json:"personalCreditBalance,omitempty"`
	// 个人现金余额
	PersonalCashBalance string `json:"personalCashBalance,omitempty"`
	// 交易金额
	Amount string `json:"amount,omitempty"`
	// 交易类型
	DealType string `json:"dealType,omitempty"`
	// 支付类型
	PayType string `json:"payType,omitempty"`
	// 交易单号
	TradeNo string `json:"tradeNo,omitempty"`
	// 结算单号
	CheckNo string `json:"checkNo,omitempty"`
	// 创建时间
	Created string `json:"created,omitempty"`
	// 公钥
	Upk string `json:"upk,omitempty"`
	// 签名
	Sign string `json:"sign,omitempty"`
}

/*
 结算单
*/
type CheckSlip struct {
	// 结算单号
	CheckNo string `json:"checkNo,omitempty"`
	// 企业社会信用代码
	CompanySocialCreditCode string `json:"companySocialCreditCode,omitempty"`
	// 商家社会信用代码
	BusinessSocialCreditCode string `json:"businessSocialCreditCode,omitempty"`
	// 结算总金额
	TotalAmount string `json:"totalAmount,omitempty"`
	// 结算总条数
	TotalDeal string `json:"totalDeal,omitempty"`
	// 结算类型(信用/现金)
	CheckType string `json:"checkType,omitempty"`
	// 结算单关联交易单号
	TradeNos []string `json:"tradeNos,omitempty"`
	// 创建时间
	Created string `json:"created,omitempty"`
	// 公钥
	Upk string `json:"upk,omitempty"`
	// 签名
	Sign string `json:"sign,omitempty"`
}

func main() {
	err := shim.Start(new(SimpleChainCode))
	if err != nil {
		fmt.Printf("Error starting Simple chaincode: %s", err)
	} else {
		fmt.Println("SimpleChainCode successfully started")
	}
}

func (t *SimpleChainCode) Init(stub shim.ChaincodeStubInterface) peer.Response {
	fmt.Println("Init")
	return shim.Success(nil)
}

func (t *SimpleChainCode) Query(stub shim.ChaincodeStubInterface) peer.Response {
	fmt.Println("Query")
	return shim.Success(nil)
}

func (t *SimpleChainCode) Invoke(stub shim.ChaincodeStubInterface) peer.Response {
	fmt.Println("Invoke")
	function, args := stub.GetFunctionAndParameters()

	switch function {
	case "saveDealingRecord":
		response := t.saveDealingRecord(stub, args)
		if response.Status == 200 {
			return shim.Success(nil)
		}

		return shim.Error(response.Message)
	case "saveCheckSlipInfo":
		response := t.saveCheckSlipInfo(stub, args)
		if response.Status == 200 {
			return shim.Success(nil)
		}
		return shim.Error(response.Message)
	case "queryDealingRecord":
		response := t.queryDealingRecord(stub, args[0])

		var order Order
		err := json.Unmarshal(response.Payload, &order)
		if err != nil {
			fmt.Println(err)
		} else {
			return response
		}
	case "queryPersonalWallet":
		response := t.queryPersonalWallet(stub, args[0])

		var personalWallet PersonalWallet
		err := json.Unmarshal(response.Payload, &personalWallet)
		if err != nil {
			fmt.Println(err)
		} else {
			fmt.Println("queryPersonalWallet result:" + string(response.Payload))
			return response
		}
	case "queryBusinessWallet":
		response := t.queryBusinessWallet(stub, args[0])

		var businessWallet BusinessWallet
		err := json.Unmarshal(response.Payload, &businessWallet)
		if err != nil {
			fmt.Println(err)
		} else {
			fmt.Println("queryBusinessWallet result:" + string(response.Payload))
			return response
		}
	case "queryCheckSlip":
		response := t.queryCheckSlip(stub, args[0])

		var checkSlip CheckSlip
		err := json.Unmarshal(response.Payload, &checkSlip)
		if err != nil {
			fmt.Println(err)
		} else {
			return response
		}
	}

	return shim.Error("Invalid invoke function name: " + function)
}

// 根据交易单号查询交易记录
func (t *SimpleChainCode) queryDealingRecord(stub shim.ChaincodeStubInterface, tradeNo string) peer.Response {
	objectBytes, err := stub.GetState(tradeNo)

	if err != nil {
		return shim.Error("Query dealingRecord err.")
	}

	return shim.Success(objectBytes)
}

// 根据个人钱包号获取钱包信息
func (t *SimpleChainCode) queryPersonalWallet(stub shim.ChaincodeStubInterface, personalWalletNo string) peer.Response {
	objectBytes, err := stub.GetState(personalWalletNo)
	if err != nil {
		return shim.Error("Query personalWallet err.")
	}

	return shim.Success(objectBytes)
}

// 根据商家钱包号获取钱包信息
func (t *SimpleChainCode) queryBusinessWallet(stub shim.ChaincodeStubInterface, businessSocialCreditCode string) peer.Response {
	objectBytes, err := stub.GetState(businessSocialCreditCode)
	if err != nil {
		return shim.Error("Query businessWallet err.")
	}

	return shim.Success(objectBytes)
}

//
func (t *SimpleChainCode) queryCheckSlip(stub shim.ChaincodeStubInterface, checkNo string) peer.Response {
	objectBytes, err := stub.GetState(checkNo)
	if err != nil {
		return shim.Error("Query checkSlip err.")
	}

	return shim.Success(objectBytes)
}

// 上传交易记录(包括订单信息、个人钱包信息、商家钱包信息)
func (t *SimpleChainCode) saveDealingRecord(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	decimal.DivisionPrecision = 2

	response := t.saveOrderInfo(stub, args[0])
	if response.Status != 200 {
		return shim.Error(response.Message)
	}

	response1 := t.savePersonalWalletInfo(stub, args[1])
	if response1.Status != 200 {
		return shim.Error(response.Message)
	}

	if len(args) > 2 {
		response2 := t.saveBusinessWalletInfo(stub, args[2])
		if response2.Status != 200 {
			return shim.Error(response.Message)
		}
	}

	return shim.Success(nil)
}

// 上传订单信息
func (t *SimpleChainCode) saveOrderInfo(stub shim.ChaincodeStubInterface, jsonString string) peer.Response {
	var order Order
	err := json.Unmarshal([]byte(jsonString), &order)
	if err != nil {
		return shim.Error("Order jsonString unmarshal err.")
	}

	fmt.Println("订单上链请求数据：" + jsonString)

	// 交易单号
	tradeNo := order.TradeNo
	if tradeNo == "" {
		return shim.Error("Order tradeNo not appearance")
	}

	r, _ := verify(order.Upk, order.Sign, tradeNo)
	if !r {
		return shim.Error("Verify false of order.")
	}

	oString, _ := json.Marshal(order)
	stub.PutState(tradeNo, oString)

	return shim.Success(nil)
}

// 上传个人钱包信息
func (t *SimpleChainCode) savePersonalWalletInfo(stub shim.ChaincodeStubInterface, jsonString string) peer.Response {
	var personalWallet PersonalWallet
	err := json.Unmarshal([]byte(jsonString), &personalWallet)
	if err != nil {
		return shim.Error("PersonalWallet jsonString unmarshal err.")
	}

	fmt.Println("个人钱包上链请求数据：" + jsonString)

	// 钱包号
	personalWalletNo := personalWallet.PersonalWalletNo
	if personalWalletNo == "" {
		return shim.Error("PersonalWalletNo not appearance")
	}

	// 交易单号
	tradeNo := personalWallet.TradeNo
	if tradeNo == "" {
		return shim.Error("PersonalWallet tradeNo not appearance")
	}

	r, _ := verify(personalWallet.Upk, personalWallet.Sign, personalWalletNo)
	if !r {
		return shim.Error("verify false of personalWallet.")
	}

	creditLimitFloat, _ := strconv.ParseFloat(personalWallet.PersonalCreditLimit, 64)
	creditLimit := decimal.NewFromFloat(creditLimitFloat)

	creditBalanceFloat, _ := strconv.ParseFloat(personalWallet.PersonalCreditBalance, 64)
	creditBalance := decimal.NewFromFloat(creditBalanceFloat)

	cashBalanceFloat, _ := strconv.ParseFloat(personalWallet.PersonalCashBalance, 64)
	cashBalance := decimal.NewFromFloat(cashBalanceFloat)

	response := t.queryPersonalWallet(stub, personalWalletNo)
	if response.Payload != nil {
		var oldPersonalWallet PersonalWallet
		erro := json.Unmarshal(response.Payload, &oldPersonalWallet)

		if erro == nil {
			fmt.Println("查询出的个人钱包数据：" + string(response.Payload))

			amountFloat, _ := strconv.ParseFloat(personalWallet.Amount, 64)
			amount := decimal.NewFromFloat(amountFloat)

			oldCreditLimitFloat, _ := strconv.ParseFloat(oldPersonalWallet.PersonalCreditLimit, 64)
			oldCreditLimit := decimal.NewFromFloat(oldCreditLimitFloat)

			oldCreditBalanceFloat, _ := strconv.ParseFloat(oldPersonalWallet.PersonalCreditBalance, 64)
			oldCreditBalance := decimal.NewFromFloat(oldCreditBalanceFloat)

			oldCashBalanceFloat, _ := strconv.ParseFloat(oldPersonalWallet.PersonalCashBalance, 64)
			oldCashBalance := decimal.NewFromFloat(oldCashBalanceFloat)

			if personalWallet.DealType == RECHARGE {
				if personalWallet.PayType == CREDIT {
					if !oldCreditBalance.Add(amount).Equal(creditBalance) || oldCreditBalance.Add(amount).GreaterThan(creditLimit) {
						fmt.Println("failed to credit recharge")
						return shim.Error("failed to credit recharge")
					}
					personalWallet.PersonalCashBalance = oldCashBalance.String()
				}

				if personalWallet.PayType == CASH {
					if !oldCashBalance.Add(amount).Equal(cashBalance) || oldCashBalance.Add(amount).LessThan(decimal.NewFromFloat(0)) {
						fmt.Println("failed to cash recharge")
						return shim.Error("failed to cash recharge")
					}
					personalWallet.PersonalCreditLimit = oldCreditLimit.String()
					personalWallet.PersonalCreditBalance = oldCreditBalance.String()
				}
			}

			if personalWallet.DealType == CONSUME {
				if personalWallet.PayType == CREDIT {
					if !oldCreditBalance.Sub(amount).Equal(creditBalance) || oldCreditBalance.Sub(amount).LessThan(decimal.NewFromFloat(0)) || !oldCreditLimit.Equal(creditLimit) || amount.GreaterThan(creditLimit) {
						fmt.Println("failed to credit consume")
						return shim.Error("failed to credit consume")
					}
					personalWallet.PersonalCashBalance = oldCashBalance.String()
				}

				if personalWallet.PayType == CASH {
					if !oldCashBalance.Sub(amount).Equal(cashBalance) || oldCashBalance.Sub(amount).LessThan(decimal.NewFromFloat(0)) {
						fmt.Println("failed to cash consume")
						return shim.Error("failed to cash consume")
					}
					personalWallet.PersonalCreditLimit = oldCreditLimit.String()
					personalWallet.PersonalCreditBalance = oldCreditBalance.String()
				}
			}

			// 如果是信用额度调整操作，amount表示新的信用额度
			if personalWallet.DealType == LIMIT_CHANGE {
				if amount.GreaterThan(oldCreditLimit) {
					personalWallet.PersonalCreditBalance = oldCreditBalance.Add(amount.Sub(oldCreditLimit)).String()
					personalWallet.PersonalCreditLimit = amount.String()
				} else {
					personalWallet.PersonalCreditBalance = oldCreditBalance.Sub(oldCreditLimit.Sub(amount)).String()
				}
				personalWallet.PersonalCashBalance = oldCashBalance.String()
			}
		} else {
			return shim.Error("SavePersonalWalletInfo fail.")
		}
	} else {
		if personalWallet.DealType == CONSUME {
			return shim.Error("failed to consume")
		}

		if personalWallet.DealType == RECHARGE && creditBalanceFloat > creditLimitFloat {
			return shim.Error("failed to recharge")
		}

		// 如果是信用额度调整操作，amount表示新的信用额度
		if personalWallet.DealType == LIMIT_CHANGE {
			personalWallet.PersonalCreditBalance = personalWallet.Amount
			personalWallet.PersonalCreditLimit = personalWallet.Amount
		}
	}

	oString, _ := json.Marshal(personalWallet)
	stub.PutState(personalWalletNo, oString)

	return shim.Success(nil)
}

// 上传商家钱包信息
func (t *SimpleChainCode) saveBusinessWalletInfo(stub shim.ChaincodeStubInterface, jsonString string) peer.Response {
	var businessWallet BusinessWallet
	err := json.Unmarshal([]byte(jsonString), &businessWallet)
	if err != nil {
		return shim.Error("BusinessWallet jsonString unmarshal err.")
	}

	fmt.Println("商家钱包上链请求数据：" + jsonString)

	// 商家社会信用码
	businessSocialCreditCode := businessWallet.BusinessSocialCreditCode
	if businessSocialCreditCode == "" {
		return shim.Error("BusinessSocialCreditCode not appearance")
	}
	// 交易单号
	tradeNo := businessWallet.TradeNo
	if tradeNo == "" {
		return shim.Error("TradeNo not appearance")
	}

	r, _ := verify(businessWallet.Upk, businessWallet.Sign, businessSocialCreditCode)
	if !r {
		return shim.Error("Verify false of businessWallet")
	}

	response := t.queryBusinessWallet(stub, businessSocialCreditCode)
	var oldBusinessWallet BusinessWallet
	erro := json.Unmarshal(response.Payload, &oldBusinessWallet)

	if erro == nil {
		fmt.Println("查询出的商家钱包数据：" + string(response.Payload))

		amountFloat, _ := strconv.ParseFloat(businessWallet.Amount, 64)
		amount := decimal.NewFromFloat(amountFloat)

		oldCashBalanceFloat, _ := strconv.ParseFloat(oldBusinessWallet.BusinessCashBalance, 64)
		oldCashBalance := decimal.NewFromFloat(oldCashBalanceFloat)

		oldCreditBalanceFloat, _ := strconv.ParseFloat(oldBusinessWallet.BusinessCreditBalance, 64)
		oldCreditBalance := decimal.NewFromFloat(oldCreditBalanceFloat)

		if businessWallet.PayType == CASH {
			businessWallet.BusinessCashBalance = oldCashBalance.Add(amount).String()
		}

		if businessWallet.PayType == CREDIT {
			businessWallet.BusinessCreditBalance = oldCreditBalance.Add(amount).String()
		}
	}

	oString, _ := json.Marshal(businessWallet)
	stub.PutState(businessSocialCreditCode, oString)

	return shim.Success(nil)
}

// 上传结算清单
func (t *SimpleChainCode) saveCheckSlipInfo(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}
	jsonString := args[0]
	var checkSlip CheckSlip
	err := json.Unmarshal([]byte(jsonString), &checkSlip)
	if err != nil {
		return shim.Error("")
	}

	fmt.Println("结算单上链请求数据：" + jsonString)

	// 结算单号
	checkNo := checkSlip.CheckNo
	if checkNo == "" {
		return shim.Error("CheckNo not appearance")
	}

	r, _ := verify(checkSlip.Upk, checkSlip.Sign, checkNo)
	if !r {
		return shim.Error("Verify false of check slip.")
	}

	// 如果是信用结算，则需要恢复个人钱包信用额度
	if checkSlip.CheckType == CREDIT {
		// 用于存放个人钱包号和钱包信用余额，这样做是为了处理在同一个结算单内同一个钱包有多条消费的情况
		maps := make(map[string]PersonalWallet)
		// 用于计算单个钱包在本次结算中累计恢复的额度
		amounts := make(map[string]float64)

		tradeNos := checkSlip.TradeNos
		for _, tradeNo := range tradeNos {
			objectBytes, _ := stub.GetState(tradeNo)

			var order Order
			er := json.Unmarshal(objectBytes, &order)
			if er != nil {
				return shim.Error("SaveCheckSlipInfo fail.Order unmarshal error.")
			}
			fmt.Println("结算操作查询出的订单数据：" + string(objectBytes))

			if order.PayType != CREDIT {
				fmt.Println("SaveCheckSlipInfo fail.Pay type not match.")
				return shim.Error("SaveCheckSlipInfo fail.Pay type not match.")
			}
			if order.Status == CHECKED {
				fmt.Println("SaveCheckSlipInfo fail.Order status not match.")
				return shim.Error("SaveCheckSlipInfo fail.Order status not match.")
			}

			// 修改订单状态为已结算并保存上链
			order.Status = CHECKED
			oString, _ := json.Marshal(order)
			stub.PutState(order.TradeNo, oString)

			// 交易金额
			amount, _ := strconv.ParseFloat(order.Amount, 64)

			personalWalletNo := order.EmployeeWalletNo
			personalWallet, ok := maps[personalWalletNo]
			if !ok {
				objBytes, _ := stub.GetState(personalWalletNo)
				err := json.Unmarshal(objBytes, &personalWallet)
				if err != nil {
					return shim.Error("SaveCheckSlipInfo fail.PersonalWallet unmarshal error.")
				}

				fmt.Println("结算操作查询出的个人钱包数据：" + string(objBytes))
				maps[personalWalletNo] = personalWallet
			}

			// 信用余额
			creditBalance, _ := strconv.ParseFloat(personalWallet.PersonalCreditBalance, 64)
			sum := decimal.NewFromFloat(amount).Add(decimal.NewFromFloat(creditBalance))

			personalWallet.PersonalCreditBalance = sum.String()
			total, flag := amounts[personalWalletNo]
			if flag {
				total += amount
			} else {
				total = amount
			}
			amounts[personalWalletNo] = total

			maps[personalWalletNo] = personalWallet
		}

		for personalWalletNo, personalWallet := range maps {
			personalWallet.TradeNo = ""
			personalWallet.DealType = RECOVER_CREDIT
			personalWallet.CheckNo = checkNo
			personalWallet.Created = time.Now().Format("2006-01-02 15:04:05")

			objString, _ := json.Marshal(personalWallet)
			fmt.Println("结算操作个人钱包上链数据：" + string(objString))

			stub.PutState(personalWalletNo, objString)
		}
	}

	oString, _ := json.Marshal(checkSlip)
	stub.PutState(checkNo, oString)

	return shim.Success(nil)
}

//定义签名算法
func verify(pkStr, sigStr, msgString string) (ok bool, err error) {
	pkBytes, err := hex.DecodeString(pkStr)
	if err != nil {
		return false, fmt.Errorf("incorrect pk format, %s", err)
	}
	sigBytes, err := hex.DecodeString(sigStr)
	if err != nil {
		return false, fmt.Errorf("incorrect signature format, %s", err)
	}
	msgBytes := []byte(msgString)
	x, y := elliptic.Unmarshal(curve, pkBytes)
	pubilcKey := &sm2.PublicKey{curve, x, y}
	sig := new(Signature)
	_, error := asn1.Unmarshal(sigBytes, sig)
	if error != nil {
		return false, error
	}

	result := sm2.Verify(pubilcKey, msgBytes, sig.R, sig.S)

	return result, nil
}

var curve = P256Sm2()

type p256Curve struct {
	*elliptic.CurveParams
}

var p256Sm2Params *elliptic.CurveParams
var p256sm2Curve p256Curve
var initonce sync.Once

type Signature struct {
	R, S *big.Int
}

// 取自elliptic的p256.go文件，修改曲线参数为sm2
// See FIPS 186-3, section D.2.3
func initP256Sm2() {
	p256Sm2Params = &elliptic.CurveParams{Name: "SM2-P-256"} // 注明为SM2
	//SM2椭 椭 圆 曲 线 公 钥 密 码 算 法 推 荐 曲 线 参 数
	p256Sm2Params.P, _ = new(big.Int).SetString("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF", 16)
	p256Sm2Params.N, _ = new(big.Int).SetString("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123", 16)
	p256Sm2Params.B, _ = new(big.Int).SetString("28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93", 16)
	p256Sm2Params.Gx, _ = new(big.Int).SetString("32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7", 16)
	p256Sm2Params.Gy, _ = new(big.Int).SetString("BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0", 16)
	p256Sm2Params.BitSize = 256

	p256sm2Curve = p256Curve{p256Sm2Params}
}

func P256Sm2() elliptic.Curve {
	initonce.Do(initP256Sm2)
	return p256sm2Curve
}
