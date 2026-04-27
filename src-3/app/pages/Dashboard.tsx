import { Link } from "react-router";
import { User, Plus, TrendingUp, Trophy, Calendar as CalendarIcon } from "lucide-react";
import { Card } from "../components/ui/card";
import { Button } from "../components/ui/button";
import { BarChart, Bar, XAxis, YAxis, ResponsiveContainer } from "recharts";
import { BottomNav } from "../components/BottomNav";

const activityData = [
  { day: "Mon", duration: 45 },
  { day: "Tue", duration: 60 },
  { day: "Wed", duration: 30 },
  { day: "Thu", duration: 50 },
  { day: "Fri", duration: 70 },
  { day: "Sat", duration: 40 },
  { day: "Sun", duration: 55 },
];

export function Dashboard() {
  return (
    <>
      <div className="min-h-screen p-6 pb-24">
        <div className="max-w-4xl mx-auto space-y-6">
          {/* Header */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-full bg-primary flex items-center justify-center">
                <User className="w-6 h-6 text-primary-foreground" />
              </div>
              <div>
                <h2 className="text-white">Welcome back!</h2>
                <p className="text-muted-foreground">Ready to train?</p>
              </div>
            </div>
            <Link to="/profile">
              <Button variant="ghost" size="icon" className="text-white hover:bg-card">
                <User className="w-5 h-5" />
              </Button>
            </Link>
          </div>

          {/* Log Workout Button */}
          <Link to="/activity">
            <Button className="w-full bg-primary hover:bg-primary/90 text-primary-foreground h-14">
              <Plus className="w-5 h-5 mr-2" />
              Log Workout
            </Button>
          </Link>

          {/* Activity Charts */}
          <Card className="bg-card border-border p-6">
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="text-white">Activity Charts</h3>
                <Link to="/progress">
                  <Button variant="ghost" size="sm" className="text-primary hover:bg-card/50">
                    View All
                  </Button>
                </Link>
              </div>
              
              <ResponsiveContainer width="100%" height={200}>
                <BarChart data={activityData}>
                  <XAxis 
                    dataKey="day" 
                    stroke="#A1A1AA"
                    tick={{ fill: '#A1A1AA' }}
                  />
                  <YAxis 
                    stroke="#A1A1AA"
                    tick={{ fill: '#A1A1AA' }}
                  />
                  <Bar dataKey="duration" fill="#00F5D4" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
              
              <p className="text-muted-foreground text-center">
                Weekly workout duration (minutes)
              </p>
            </div>
          </Card>

          {/* Quick Links */}
          <div className="grid grid-cols-2 gap-4">
            <Link to="/progress">
              <Card className="bg-card border-border p-6 hover:bg-card/80 transition-colors cursor-pointer h-full">
                <div className="space-y-3">
                  <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
                    <TrendingUp className="w-5 h-5 text-primary" />
                  </div>
                  <div>
                    <h4 className="text-white">Progress</h4>
                    <p className="text-muted-foreground">Track your gains</p>
                  </div>
                </div>
              </Card>
            </Link>

            <Link to="/calendar">
              <Card className="bg-card border-border p-6 hover:bg-card/80 transition-colors cursor-pointer h-full">
                <div className="space-y-3">
                  <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
                    <CalendarIcon className="w-5 h-5 text-primary" />
                  </div>
                  <div>
                    <h4 className="text-white">Calendar</h4>
                    <p className="text-muted-foreground">View schedule</p>
                  </div>
                </div>
              </Card>
            </Link>
          </div>

          {/* Competitions */}
          <Card className="bg-card border-border p-6">
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="text-white">Competitions</h3>
                <Trophy className="w-5 h-5 text-primary" />
              </div>
              <div className="space-y-3">
                <div className="flex items-center justify-between p-3 bg-background rounded-lg">
                  <div>
                    <p className="text-white">30-Day Challenge</p>
                    <p className="text-muted-foreground">15 days remaining</p>
                  </div>
                  <div className="text-primary">50%</div>
                </div>
                <div className="flex items-center justify-between p-3 bg-background rounded-lg">
                  <div>
                    <p className="text-white">Monthly Miles</p>
                    <p className="text-muted-foreground">23.5 / 50 miles</p>
                  </div>
                  <div className="text-primary">47%</div>
                </div>
              </div>
            </div>
          </Card>
        </div>
      </div>
      <BottomNav />
    </>
  );
}