//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ddd.core.filter;


import ddd.core.Response;
import ddd.core.exception.BizException;
import ddd.core.exception.SysException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler({SysException.class})
    public Response handleGlobalException(HttpServletRequest request, SysException exception) {
        return Response.buildFailure(exception.getErrCode(), exception.getMessage());
    }
    @ExceptionHandler({BizException.class})
    public Response handleBizException(HttpServletRequest request, BizException exception) {
        return Response.buildFailure(exception.getErrCode(), exception.getMessage());
    }
}
