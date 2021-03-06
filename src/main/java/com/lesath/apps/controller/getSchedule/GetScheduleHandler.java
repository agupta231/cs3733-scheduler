package com.lesath.apps.controller.getSchedule;

/**
 * @author Ankur without the red squiggly
 */

import com.lesath.apps.controller.APIGatewayRequest;
import com.lesath.apps.controller.LambdaHandler;
import com.lesath.apps.db.ScheduleDAO;
import com.lesath.apps.model.Schedule;
import com.lesath.apps.util.HTTPMethod;

import java.util.ArrayList;

public class GetScheduleHandler extends LambdaHandler {
	@Override
	protected boolean handleGET(APIGatewayRequest request) {
    	String uuid = request.queryStringParameters.get("uuid");

    	try {
    		ScheduleDAO dao = new ScheduleDAO();
    		Schedule schedule = dao.getSchedule(uuid);

			if(schedule != null) {
				GetScheduleGETResponse res = new GetScheduleGETResponse(schedule);

				if (!res.loadMeetingsAndTimesNotAvailable()) {
					this.response.setStatusCode(500);
					return false;
				}

				res.generateRequest();

				this.response.setBody(gson.toJson(res));
				this.response.setStatusCode(200);
			} else {
				this.response.setStatusCode(204);
			}

		} catch (Exception e) {
			e.printStackTrace();
			this.response.setStatusCode(500);
			return false;
		}

		return true;
	}

	@Override
	protected boolean init() {
		this.controllerName = "GetSchedule";

		this.handledMethods = new ArrayList<>();
		this.handledMethods.add(HTTPMethod.GET);

		return true;
	}

	@Override
	protected boolean handlePUT(APIGatewayRequest request) {
		return false;
	}

	@Override
	protected boolean handlePOST(APIGatewayRequest request) {
		return false;
	}

	@Override
	protected boolean handleDELETE(APIGatewayRequest request) {
		return false;
	}

}
