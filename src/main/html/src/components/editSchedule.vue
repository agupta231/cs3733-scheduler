<template>
	<div id='app'>
		<h1>{{ schedule.name }}</h1>
		<button :disabled='page == 0' class='button' @click='page--'>Previous Page</button>
		<button :disabled='atEnd' class='button' @click='page++'>Next Page</button>
		<button class='button is-danger' @click='deleteSchedule()'>Delete</button>
		<table class='table is-bordered'>
			<tbody>
				<tr>
					<th>
						{{ dateRange }}</br>
						<button @click='extend(-1)'>Extend day left</button>
						<button @click='extend(1)'>Extend day right</button>
					</th>
					<th>
						Monday</br>
						<button @click='openDay(0)'>Open</button>
						<button @click='cancelDay(0)'>Close</button>
					</th>
					<th>
						Tuesday</br>
						<button @click='openDay(1)'>Open</button>
						<button @click='cancelDay(1)'>Close</button>
					</th>
					<th>
						Wednesday</br>
						<button @click='openDay(2)'>Open</button>
						<button @click='cancelDay(2)'>Close</button>
					</th>
					<th>
						Thursday</br>
						<button @click='openDay(3)'>Open</button>
						<button @click='cancelDay(3)'>Close</button>
					</th>
					<th>
						Friday</br>
						<button @click='openDay(4)'>Open</button>
						<button @click='cancelDay(4)'>Close</button>
					</th>
				</tr>
				<tr v-for='time, index in currentWeek'>
					<td>
						{{ time.time }}
						<button @click='openTime(index)'>Open</button>
						<button @click='cancelTime(index)'>Close</button>
					</td>
					<td v-for='timeSlot in time.days' v-bind:class="[ 'noPadding', !timeSlot.inBounds ? 'notInBounds' : '' ]">
						<div v-if='timeSlot.inBounds && !timeSlot.name' class='columns noMargin' @click='toggleTimeslot(timeSlot)'>
							<div v-bind:class='[ "noselect", "column", timeSlot.available ? "has-background-success" : "has-background-grey-light"  ]'>
								Open
							</div>
							<div v-bind:class='[ "noselect", "column", !timeSlot.available ? "has-background-danger" : "has-background-grey-light"  ]'>
								Close
							</div>
						</div>
						<div v-if='timeSlot.inBounds && timeSlot.name'>
							<p class='has-text-centered'>{{ timeSlot.name }}</p>
							<button @click='cancelMeeting(timeSlot)'>cancel</button>
						</div>
					</td>
				</tr>
			</tbody>
		</table>
	</div>
</template>
<script>

export default {
	data: function() {
		return {
			schedule: {},
			times: [],
			uuid: null,
			dateRange: null,
			newMeeting: {},
			page: 0
		}
	},
	computed: {
		currentWeek: function () {
			let currentWeek = [];
			for (let i = 0; i < this.times.length; i ++) {
				currentWeek.push({
					days: this.times[i].days.slice(this.page*5,this.page*5 + 5),
					time: this.times[i].time
				});
			}
			if(currentWeek.length > 0){
				let startDate = new Date(currentWeek[0].days[0].day);
				let endDate = new Date(currentWeek[0].days[4].day);
				this.dateRange = '(' + (startDate.getMonth() + 1) + '/' + startDate.getDate() + '-' + (endDate.getMonth() + 1) + '/' + endDate.getDate() + ')';
			}
			return currentWeek;
		},
		atEnd: function () {
			if(this.times.length != 0){
				return this.page + 1 == this.times[0].days.length/5;
			} else {
				return false;
			}
		},
		meetings: function () {
			let temp = {};
			if(this.schedule.meetings){
				for(let i = 0; i < this.schedule.meetings.length; i++){
					temp[this.schedule.meetings[i].startTime] = {
						name: this.schedule.meetings[i].participantName,
						id: this.schedule.meetings[i].id
					}
				}
			}
			return temp;
		},
		notAviable: function () {
			let temp = {};
			if(this.schedule.timesNotAvailable){
				for(let i = 0; i < this.schedule.timesNotAvailable.length; i++){
					temp[this.schedule.timesNotAvailable[i]] = true;
				}
			}
			return temp;
		}
	},
	methods: {
		async getSchedule () {
			await fetch('https://wasu526ybc.execute-api.us-east-2.amazonaws.com/Zeta/participantSchedule?uuid=' + this.uuid)
			.then(res => res.json())
        	.then(response => this.schedule = response)
        	.catch(error => console.error('Error:', error));
		},
		parseSchedule () {
			let startDate = new Date(this.schedule.startDateTime);
			let endDate = new Date(this.schedule.endDateTime);

			let startMinutes = (startDate.getHours() * 60) + startDate.getMinutes();
			let endMinutes = (endDate.getHours() * 60) + endDate.getMinutes();

			for (let i = startMinutes; i < endMinutes; i = i + this.schedule.meetingDuration) {
				let temp = [];
				let cursorDate = new Date(this.schedule.startDateTime);
				let lastDate = new Date(this.schedule.endDateTime);
				cursorDate.setHours(0,0);
				lastDate.setHours(23,59);

				//Sets cursor to Monday of first week
				var day = cursorDate.getDay() || 7;
				if ( day !== 1 ) {             
				    cursorDate.setHours(-24 * (day - 1));
				}

				//Sets last date to Friday of last week
				var day = lastDate.getDay() || 7;
				if ( day !== 5 ) {             
				    lastDate.setHours(24 * (5-day));
				}
				while (cursorDate <= lastDate) {
					let dateString = (cursorDate.getYear()+1900) + '-' + ('0' + (cursorDate.getMonth()+1)).slice(-2) + '-' + String("0" + cursorDate.getDate()).slice(-2) + 'T' + String('0' + Math.floor(i/60)).slice(-2) + ':' + (i%60 == 0 ? '00': (i%60 == 5 ? '05' : i%60)) + ':' + String('0' + cursorDate.getSeconds()).slice(-2) + '.000' + 'Z';
					if (cursorDate >= new Date(startDate).setHours(0,0,0) && cursorDate <= new Date(endDate.setHours(23,59,59))) {
						if(this.meetings[dateString]){
							temp.push({
								day: cursorDate.toString(),
								time: i,
								inBounds: true,
								available: false,
								name: this.meetings[dateString].name,
								meetingId: this.meetings[dateString].id
							})
						} else if (this.notAviable[dateString]){
							temp.push({
								day: cursorDate.toString(),
								time: i,
								inBounds: true,
								available: false,
							})
						} else {
							temp.push({
								day: cursorDate.toString(),
								time: i,
								inBounds: true,
								available: true
							})	
						}
					} else {
						temp.push({
							day: cursorDate.toString(),
							inBounds: false,
							available: false,
						});
					}
					if (cursorDate.getDay() == 5){
						cursorDate.setDate(cursorDate.getDate() + 3);
					} else {
						cursorDate.setDate(cursorDate.getDate() + 1);
					}
				}

				this.times.push({
					time: Math.floor(i/60) + ':' + (i%60 == 0 ? '00': (i%60 == 5 ? '05' : i%60)),
					days: temp
				});
			}
		},
		async toggleTimeslot (timeslot) {
			timeslot.available = !timeslot.available;
			let startTime = new Date(timeslot.day);
			startTime.setUTCHours(Math.floor(timeslot.time/60), timeslot.time%60)
			let body = {
				startTime,
				status: timeslot.available
			}
			await fetch('https://wasu526ybc.execute-api.us-east-2.amazonaws.com/Zeta/toggleTimeslot?scheduleId=' + this.uuid,{
				method: 'POST',
				body: JSON.stringify(body),
				headers:{
		          'Content-Type': 'application/json'
		        }
			})
			.then(res => res.json())
	        .catch(error => console.error('Error:', error));
	        location.reload();
		},
		async cancelMeeting (timeSlot) {
			console.log(timeSlot)
			await fetch('https://wasu526ybc.execute-api.us-east-2.amazonaws.com/Zeta/cancelorgmeeting?scheduleId=' + this.uuid + "&meetingId=" + timeSlot.meetingId, {
		        method: 'DELETE',
		        headers:{
		          'Content-Type': 'application/json'
		        }
		      })
		        .catch(error => console.error('Error:', error));
		    location.reload();
		},
		async cancelDay (dayOfWeek) {
			let temp = [];
			for (let i = 0; i < this.currentWeek.length; i++) {
				let date = new Date(this.currentWeek[i].days[dayOfWeek].day);
				let time = this.currentWeek[i].days[dayOfWeek].time;
				date.setUTCHours(Math.floor(time/60), time%60)
				temp.push(date);
			}
			let body = {
				scheduleId: this.uuid,
				times: temp
			}
			await fetch('https://wasu526ybc.execute-api.us-east-2.amazonaws.com/Zeta/multitoggletimeslot',{
				method: 'PUT',
				body: JSON.stringify(body),
				headers:{
		          'Content-Type': 'application/json'
		        }
			})
			.then(res => res.json())
	        .catch(error => console.error('Error:', error));
	        location.reload();
		},
		async cancelTime (index) {
			let days = this.currentWeek[index].days;
			let temp = [];
			for (let i = 0; i < days.length; i++) {
				if (days[i].inBounds) {
					let date = new Date(days[i].day);
					let time = days[i].time;
					date.setUTCHours(Math.floor(time/60), time%60)
					temp.push(date)
				}
			}
			let body = {
				scheduleId: this.uuid,
				times: temp
			}
			await fetch('https://wasu526ybc.execute-api.us-east-2.amazonaws.com/Zeta/multitoggletimeslot',{
				method: 'PUT',
				body: JSON.stringify(body),
				headers:{
		          'Content-Type': 'application/json'
		        }
			})
			.then(res => res.json())
	        .catch(error => console.error('Error:', error));
	        location.reload();
		},
		async openDay (dayOfWeek) {
			let temp = [];
			for (let i = 0; i < this.currentWeek.length; i++) {
				let date = new Date(this.currentWeek[i].days[dayOfWeek].day);
				let time = this.currentWeek[i].days[dayOfWeek].time;
				date.setUTCHours(Math.floor(time/60), time%60)
				temp.push(date);
			}
			let body = {
				scheduleId: this.uuid,
				times: temp
			}
			await fetch('https://wasu526ybc.execute-api.us-east-2.amazonaws.com/Zeta/multitoggletimeslot',{
				method: 'DELETE',
				body: JSON.stringify(body),
				headers:{
		          'Content-Type': 'application/json'
		        }
			})
			.then(res => res.json())
	        .catch(error => console.error('Error:', error));
	        location.reload();
		},
		async openTime (index) {
			let days = this.currentWeek[index].days;
			let temp = [];
			for (let i = 0; i < days.length; i++) {
				if (days[i].inBounds) {
					let date = new Date(days[i].day);
					let time = days[i].time;
					date.setUTCHours(Math.floor(time/60), time%60)
					temp.push(date)
				}
			}
			let body = {
				scheduleId: this.uuid,
				times: temp
			}
			await fetch('https://wasu526ybc.execute-api.us-east-2.amazonaws.com/Zeta/multitoggletimeslot',{
				method: 'DELETE',
				body: JSON.stringify(body),
				headers:{
		          'Content-Type': 'application/json'
		        }
			})
			.then(res => res.json())
	        .catch(error => console.error('Error:', error));
	        location.reload();
		},
		async extend (deltaDays) {
			let body = {
				deltaDays
			}
			await fetch('https://wasu526ybc.execute-api.us-east-2.amazonaws.com/Zeta/extendSchedule?scheduleId=' + this.uuid,{
				method: 'POST',
				body: JSON.stringify(body),
				headers:{
		          'Content-Type': 'application/json'
		        }
			})
			.then(res => res.json())
	        .catch(error => console.error('Error:', error));
	        location.reload();
		},
		async deleteSchedule () {
			await fetch('https://wasu526ybc.execute-api.us-east-2.amazonaws.com/Zeta/organizerSchedule?scheduleId=' + this.uuid,{
				method: 'DELETE',
				headers:{
		          'Content-Type': 'application/json'
		        }
			})
			.then(res => res.json())
	        .catch(error => console.error('Error:', error));
	        location.reload();
		}
	},
	async created () {
		this.uuid = this.$route.query.uuid;
		await this.getSchedule();
		this.parseSchedule();
	}
}
</script>
<style>
.notInBounds {
	background-color: gray
}
.noMargin { 
	margin: 0px !important;
}
.noPadding {
	padding: 0px !important;
}
.noselect {
  -webkit-touch-callout: none; /* iOS Safari */
    -webkit-user-select: none; /* Safari */
     -khtml-user-select: none; /* Konqueror HTML */
       -moz-user-select: none; /* Firefox */
        -ms-user-select: none; /* Internet Explorer/Edge */
            user-select: none; /* Non-prefixed version, currently
                                  supported by Chrome and Opera */
}
</style>