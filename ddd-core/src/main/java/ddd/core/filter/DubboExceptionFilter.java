package ddd.core.filter;

import ddd.core.Response;
import ddd.core.exception.BizException;
import ddd.core.exception.SysException;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.filter.ExceptionFilter;
import org.apache.dubbo.rpc.service.GenericService;

/**
 * dubbo全局异常拦截器, 统一Response出参
 */
@Activate(group = CommonConstants.PROVIDER)
public class DubboExceptionFilter extends ExceptionFilter {
    private static Logger logger = LoggerFactory.getLogger(ExceptionFilter.class);

    private static boolean isDDDResponse(Class returnType) {
        return returnType == Response.class || returnType.getGenericSuperclass() == Response.class;
    }

    private static Object handleDDDResponse(Class returnType, String errCode, String errMsg) {
        try {
            Response response = (Response) returnType.newInstance();
            response.setSuccess(false);
            response.setErrCode(errCode);
            response.setErrMessage(errMsg);
            return response;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        return super.invoke(invoker, invocation);
    }

    @Override
    public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
        if (appResponse.hasException() && GenericService.class != invoker.getInterface()) {
            Class returnType = getReturnType(invoker, invocation);
            if (isDDDResponse(returnType)) {
                Throwable e = appResponse.getException();
                if (e instanceof BizException) {
                    BizException bizException = (BizException) e;
                    logger.warn("BIZ EXCEPTION : " + e.getMessage());
                    //在Debug的时候，对于BizException也打印堆栈
                    if (logger.isDebugEnabled()) {
                        logger.error(e.getMessage(), e);
                    }
                    Object response = handleDDDResponse(returnType, bizException.getErrCode(), bizException.getMessage());
                    appResponse.setValue(response);
                    appResponse.setException(null);
                }

                if (e instanceof SysException) {
                    SysException sysException = (SysException) e;
                    logger.error("SYS EXCEPTION :");
                    logger.error(e.getMessage(), e);
                    Object response = handleDDDResponse(returnType, sysException.getErrCode(), sysException.getMessage());
                    appResponse.setValue(response);
                    appResponse.setException(null);
                }
            }
            super.onResponse(appResponse, invoker, invocation);
        }
    }

    /**
     * 获取方法返回类型
     *
     * @param invoker
     * @param invocation
     * @return
     */
    private Class<?> getReturnType(Invoker<?> invoker, Invocation invocation) {
        try {
            return invoker.getInterface().getMethod(invocation.getMethodName(),
                    invocation.getParameterTypes()).getReturnType();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}

