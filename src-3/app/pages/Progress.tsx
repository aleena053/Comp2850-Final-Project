import { Link } from "react-router";
import { ArrowLeft, User, MessageSquare, Edit } from "lucide-react";
import { Card } from "../components/ui/card";
import { Button } from "../components/ui/button";
import { LineChart, Line, XAxis, YAxis, ResponsiveContainer, Tooltip } from "recharts";
import { BottomNav } from "../components/BottomNav";

const progressData = [
  { week: "W1", weight: 180, strength: 75 },
  { week: "W2", weight: 178, strength: 78 },
  { week: "W3", weight: 177, strength: 82 },
  { week: "W4", weight: 176, strength: 85 },
  { week: "W5", weight: 175, strength: 88 },
  { week: "W6", weight: 174, strength: 90 },
];

export function Progress() {
  return (
    <>
      <div className="min-h-screen p-6 pb-24">
        <div className="max-w-4xl mx-auto space-y-6">
          <div className="flex items-center gap-4">
            <Link to="/dashboard">
              <button className="text-white hover:text-primary">
                <ArrowLeft className="w-6 h-6" />
              </button>
            </Link>
            <h1 className="text-white">Progress Tracking</h1>
          </div>

          <div className="flex items-center gap-4">
            <div className="w-16 h-16 rounded-full bg-primary flex items-center justify-center">
              <User className="w-8 h-8 text-primary-foreground" />
            </div>
            <div className="flex-1">
              <h2 className="text-white">Your Journey</h2>
              <p className="text-muted-foreground">Track your fitness progress</p>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <Card className="bg-card border-border p-4">
              <div className="space-y-1">
                <p className="text-muted-foreground">Completed Sessions</p>
                <p className="text-white">42 workouts</p>
              </div>
            </Card>
            <Card className="bg-card border-border p-4">
              <div className="space-y-1">
                <p className="text-muted-foreground">Last Active</p>
                <p className="text-white">2 hours ago</p>
              </div>
            </Card>
          </div>

          <Card className="bg-card border-border p-6">
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="text-white">Progress Chart</h3>
                <Button 
                  size="sm" 
                  variant="ghost"
                  className="text-primary hover:bg-card/50"
                >
                  <Edit className="w-4 h-4 mr-2" />
                  Edit Progress
                </Button>
              </div>
              
              <ResponsiveContainer width="100%" height={250}>
                <LineChart data={progressData}>
                  <XAxis 
                    dataKey="week" 
                    stroke="#A1A1AA"
                    tick={{ fill: '#A1A1AA' }}
                  />
                  <YAxis 
                    stroke="#A1A1AA"
                    tick={{ fill: '#A1A1AA' }}
                  />
                  <Tooltip 
                    contentStyle={{ 
                      backgroundColor: '#1C1C1E', 
                      border: '1px solid rgba(255,255,255,0.1)',
                      borderRadius: '8px',
                      color: '#FFFFFF'
                    }}
                  />
                  <Line 
                    type="monotone" 
                    dataKey="weight" 
                    stroke="#00F5D4" 
                    strokeWidth={2}
                    dot={{ fill: '#00F5D4', r: 4 }}
                  />
                  <Line 
                    type="monotone" 
                    dataKey="strength" 
                    stroke="#007A6A" 
                    strokeWidth={2}
                    dot={{ fill: '#007A6A', r: 4 }}
                  />
                </LineChart>
              </ResponsiveContainer>

              <div className="flex justify-center gap-6">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 rounded-full bg-primary"></div>
                  <span className="text-muted-foreground">Weight</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 rounded-full bg-secondary"></div>
                  <span className="text-muted-foreground">Strength</span>
                </div>
              </div>
            </div>
          </Card>

          <Card className="bg-card border-border p-6">
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="text-white">Messages & Notes</h3>
                <MessageSquare className="w-5 h-5 text-primary" />
              </div>
              <div className="space-y-3">
                <div className="p-3 bg-background rounded-lg">
                  <p className="text-white mb-1">Great progress this week!</p>
                  <p className="text-muted-foreground">Added 10 lbs to bench press</p>
                </div>
                <div className="p-3 bg-background rounded-lg">
                  <p className="text-white mb-1">Remember to stretch</p>
                  <p className="text-muted-foreground">Focus on mobility work</p>
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