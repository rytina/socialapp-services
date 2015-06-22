package com.socialapp.services.persistence;

import com.socialapp.services.dao.City;

public interface IDataSource {

	City localQueryForCityByName(String cityName, int interest, boolean b);

	void close();

}
