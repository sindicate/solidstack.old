/*--
 * Copyright 2012 René M. de Bloois
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package solidstack.hyperdb;

import java.util.List;

public class Schema
{
	public String name;
	public int tableCount;
	public int viewCount;
	protected List<Table> tables;
	protected List<View> views;

	public Schema( String name, int tableCount, int viewCount )
	{
		this.name = name;
		this.tableCount = tableCount;
		this.viewCount = viewCount;
	}

	public List<Table> getTables()
	{
		return this.tables;
	}

	public void setTables( List<Table> tables )
	{
		this.tables = tables;
	}

	public List<View> getViews()
	{
		return this.views;
	}

	public void setViews( List<View> views )
	{
		this.views = views;
	}
}
