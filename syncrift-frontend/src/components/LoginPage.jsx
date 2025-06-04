"use client";
import { useState } from 'react';
import { X } from 'lucide-react';
import { useAuth } from '../utils/AuthContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { toast } from 'sonner';

export default function LoginPopup({ onClose }) {
  const [loginIdentifier, setloginIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const { login } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await login({ loginIdentifier, password });
      toast.success('Login successful');
      onClose();
    } catch (error) {
      toast.error('Login failed', { description: error.message });
    }
  };

  return (
    <div className="fixed inset-0 z-70 flex items-center justify-center from-black/80 via-gray-900/80 to-gray-800/80 backdrop-blur-xl">
      <Card className="relative w-full max-w-md from-gray-900 via-black to-gray-900 text-white border border-gray-800">
        <Button
          variant="ghost"
          size="icon"
          className="absolute right-4 top-4 h-8 w-8 text-gray-400 hover:bg-gray-800/50"
          onClick={onClose}
        >
          <X className="h-4 w-4" />
        </Button>

        <CardHeader>
          <CardTitle className="text-3xl font-bold text-gray-100">Welcome back</CardTitle>
        </CardHeader>

        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email" className="text-white">Email</Label>
              <Input
                id="email"
                type="text"
                value={loginIdentifier}
                onChange={(e) => setloginIdentifier(e.target.value)}
                className="bg-white/10 border-white/20 text-white placeholder:text-white/70"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="password" className="text-white">Password</Label>
              <Input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="bg-white/10 border-white/20 text-white placeholder:text-white/70"
                required
              />
            </div>

            <Button 
              type="submit" 
              className="w-full mt-6 py-6 text-lg font-semibold bg-gray-100 text-gray-900 hover:bg-gray-200 transition-colors duration-200"
            >
              Sign in
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}