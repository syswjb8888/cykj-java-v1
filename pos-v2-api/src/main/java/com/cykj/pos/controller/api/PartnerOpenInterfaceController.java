package com.cykj.pos.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cykj.common.core.domain.AjaxResult;
import com.cykj.common.core.domain.entity.SysUser;
import com.cykj.pos.domain.BizMerchant;
import com.cykj.pos.enums.bizstatus.BizStatusContantEnum;
import com.cykj.pos.profit.dto.*;
import com.cykj.pos.service.*;
import com.cykj.system.service.ISysUserService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
@RequestMapping("/noauth")
public class PartnerOpenInterfaceController {

    private final IBizMerchantService iBizMerchantService;

    private  final IBizVerifyCodeService iBizVerifyCodeService;

    private final IBizPosMachineService iBizPosMachineService;

    private final IDataSecurityService dataSecurityService;

    @ApiOperation(value="发送短信验证码")
    @ApiImplicitParams({@ApiImplicitParam(name="mobile",value = "路径参数1：合法手机号码",dataType = "long",required = true,paramType="body"),
            @ApiImplicitParam(name="inviteCode",value = "邀请码，即邀请商户编码",dataType = "long",required = true,paramType="body")})
    @ApiResponses({@ApiResponse(code=200,response = AjaxResult.class,message = "业务数据响应成功")})
    @GetMapping("/verifyCode/sender/{mobile}/{inviteCode}")
    public AjaxResult verifyCodeSender(@PathVariable String mobile,@PathVariable String inviteCode){

        LambdaQueryWrapper<BizMerchant> lqw = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(inviteCode)) {
            lqw.eq(BizMerchant::getMerchCode, inviteCode);
        }
        BizMerchant merchant = iBizMerchantService.getOne(lqw);

        if(merchant == null){
           return AjaxResult.error( BizStatusContantEnum.BIZ_FAIL.getName());
        }
        BizStatusContantEnum bizStatus = iBizVerifyCodeService.sendVerifyCode(mobile, merchant.getUserId());
        if(bizStatus == BizStatusContantEnum.BIZ_SUCCESS)return AjaxResult.success("短信验证码发送成功");
        return AjaxResult.error(bizStatus.getName());
    }
    @PostMapping("/partner/register")
    public AjaxResult partnerRegister(@RequestBody PartnerInviteDTO partnerInviteDTO){
        String mobile = partnerInviteDTO.getMobile();
        String verifyCode = partnerInviteDTO.getVerifyCode();
        BizStatusContantEnum bizStatus = iBizVerifyCodeService.verifyCodeValidate(mobile,verifyCode);
        if(bizStatus != BizStatusContantEnum.SMS_SUCCESS){
            return AjaxResult.error(bizStatus.getName());
        }
        BizStatusContantEnum status = iBizMerchantService.partnerRegister(partnerInviteDTO);
        if(status != BizStatusContantEnum.PARTNER_REGISTER_SUCCESS){
            return AjaxResult.error(bizStatus.getName());
        }
        return AjaxResult.success(BizStatusContantEnum.PARTNER_REGISTER_SUCCESS.getName());
    }

    @PostMapping("/merchant/transaction/append")
    public KuaiQianResult addMerchantTransaction(@RequestBody ThirdPartRequestDataDTO<String> requestDataDTO){
        String sign = requestDataDTO.getSign();
        String data = requestDataDTO.getData();
        boolean verify = dataSecurityService.dataVerfiy(sign,data);
        if(!verify)return new KuaiQianResult("01","验证失败");
        // 工具类型
        BizMerchTransDTO merchTransDTO = JSONObject.parseObject(data,BizMerchTransDTO.class);
        // 转换成数据转换对象
        iBizMerchantService.addMerchantTransaction(merchTransDTO);
        return new KuaiQianResult("00","新增商户消费交易记录成功");
    }

    @PostMapping("/merchant/transaction/cancel")
    public KuaiQianResult cancelMerchantTransaction(@RequestBody ThirdPartRequestDataDTO<String> requestDataDTO){
        String sign = requestDataDTO.getSign();
        String data = requestDataDTO.getData();
        System.out.println("-----------测试商户消费交易记录撤消功能-----------");
        System.out.println(sign);
        System.out.println(data);
        boolean verify = dataSecurityService.dataVerfiy(sign,data);
        if(!verify)return new KuaiQianResult("01","验证失败");
        // 转换成数据转换对象
        BizCancelTransDTO merchTransDTO = JSONObject.parseObject(data,BizCancelTransDTO.class);
        iBizMerchantService.cancelMerchantTransaction(merchTransDTO);
        return new KuaiQianResult("00","商户消费交易记录撤消成功");
    }

    @PostMapping("/merchant/transaction/return")
    public KuaiQianResult returnMerchantTransaction(@RequestBody ThirdPartRequestDataDTO<String> requestDataDTO){
        String sign = requestDataDTO.getSign();
        String data = requestDataDTO.getData();
        boolean verify = dataSecurityService.dataVerfiy(sign, data);
        if(!verify)return new KuaiQianResult("01","验证失败");
        // 转换成数据转换对象
        BizReturnTransDTO merchTransDTO = JSONObject.parseObject(data,BizReturnTransDTO.class);
        iBizMerchantService.returnMerchantTransaction(merchTransDTO);
        return new KuaiQianResult("00","商户消费交易记录退货成功");
    }

    @PostMapping("/terminal/bind")
    public KuaiQianResult posMacineBind(@RequestBody ThirdPartRequestDataDTO<String> requestDataDTO){
        String sign = requestDataDTO.getSign();
        String data = requestDataDTO.getData();
        boolean verify = dataSecurityService.dataVerfiy(sign, data);
        if(!verify)return new KuaiQianResult("01","验证失败");
        // 转换成数据转换对象
        TerminalBindDTO terminalBindDTO = JSONObject.parseObject(data,TerminalBindDTO.class);
        iBizPosMachineService.posMachineBind(terminalBindDTO);
        return new KuaiQianResult("00","设备绑定信息更新成功");
    }

    @PostMapping("/terminal/activation")
    public KuaiQianResult posMacineActivate(@RequestBody ThirdPartRequestDataDTO<String> requestDataDTO){
        String sign = requestDataDTO.getSign();
        String data = requestDataDTO.getData();
        boolean verify = dataSecurityService.dataVerfiy(sign, data);
        if(!verify)return new KuaiQianResult("01","验证失败");
        TerminalActivateDTO terminalActivateDTO = JSONObject.parseObject(data,TerminalActivateDTO.class);
        // 转换成数据转换对象
        iBizPosMachineService.posMachineActivate(terminalActivateDTO);
        return new KuaiQianResult("00","设备激活信息更新成功");
    }
}
